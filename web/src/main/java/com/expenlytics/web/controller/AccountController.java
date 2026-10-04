package com.expenlytics.web.controller;

import com.expenlytics.core.exception.AccountException;
import com.expenlytics.core.model.AccountCredentials;
import com.expenlytics.core.model.AccountProfile;
import com.expenlytics.core.usecase.AccountService;
import com.expenlytics.web.security.DeviceInfoCollector;
import java.time.Duration;
import java.util.concurrent.Callable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
@RequestMapping(ApiRootController.ROOT + "/auth")
public class AccountController {

    private static final String COOKIE = "expenselytics_session";
    private final Scheduler scheduler;
    private final boolean secureCookie;
    private final AccountService accounts;
    private final DeviceInfoCollector devices;

    public AccountController(
        AccountService accounts,
        DeviceInfoCollector devices,
        @org.springframework.beans.factory.annotation.Value(
            "${app.security.secure-cookie:${APP_SECURE_COOKIE:true}}"
        ) boolean secureCookie,
        @org.springframework.beans.factory.annotation.Qualifier(
            "accountScheduler"
        ) Scheduler scheduler
    ) {
        this.scheduler = scheduler;
        this.secureCookie = secureCookie;
        this.accounts = accounts;
        this.devices = devices;
    }

    @ExceptionHandler(
        org.springframework.dao.DataIntegrityViolationException.class
    )
    ProblemDetail conflict(
        org.springframework.dao.DataIntegrityViolationException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "The record conflicts with existing data"
        );
    }

    @ExceptionHandler(AccountException.class)
    ProblemDetail accountError(AccountException exception) {
        HttpStatus status = switch (exception.reason()) {
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case DUPLICATE_EMAIL -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
        };
        return ProblemDetail.forStatusAndDetail(
            status,
            exception.getMessage()
        );
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AccountProfile> signup(
        @RequestBody AccountCredentials input,
        ServerWebExchange exchange
    ) {
        return authenticate(input, true, exchange);
    }

    @PostMapping("/login")
    public Mono<AccountProfile> login(
        @RequestBody AccountCredentials input,
        ServerWebExchange exchange
    ) {
        return authenticate(input, false, exchange);
    }

    private Mono<AccountProfile> authenticate(
        AccountCredentials input,
        boolean signup,
        ServerWebExchange exchange
    ) {
        return blocking(() -> {
            var result = accounts.authenticate(
                input,
                signup,
                devices.collect(
                    exchange.getRequest(),
                    signup ? "SIGNUP" : "LOGIN"
                )
            );
            cookie(exchange, result.token(), Duration.ofDays(7));
            return result.profile();
        }, exchange);
    }

    @GetMapping("/me")
    public Mono<AccountProfile> me(ServerWebExchange exchange) {
        return blocking(
            () -> accounts.current(token(exchange)),
            exchange
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> logout(ServerWebExchange exchange) {
        return blocking(() -> {
            accounts.logout(token(exchange));
            cookie(exchange, "", Duration.ZERO);
            return true;
        }, exchange).then();
    }

    private String token(ServerWebExchange exchange) {
        var cookie = exchange.getRequest().getCookies().getFirst(COOKIE);
        if (cookie == null) throw new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Please log in"
        );
        return cookie.getValue();
    }

    private void cookie(
        ServerWebExchange exchange,
        String value,
        Duration age
    ) {
        exchange
            .getResponse()
            .addCookie(
                ResponseCookie.from(COOKIE, value)
                    .httpOnly(true)
                    .sameSite("Strict")
                    .secure(secureCookie)
                    .path(ApiRootController.ROOT + "/auth")
                    .maxAge(age)
                    .build()
            );
    }

    private <T> Mono<T> blocking(
        Callable<T> work,
        ServerWebExchange exchange
    ) {
        exchange.getResponse().getHeaders().setCacheControl("no-store");
        return Mono.fromCallable(work)
            .subscribeOn(scheduler)
            .onErrorMap(
                java.util.concurrent.RejectedExecutionException.class,
                error ->
                    new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Please try again shortly"
                    )
            );
    }
}
