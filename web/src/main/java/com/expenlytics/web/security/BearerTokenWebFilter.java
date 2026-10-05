package com.expenlytics.web.security;

import com.expenlytics.core.exception.AccountException;
import com.expenlytics.core.usecase.AccountService;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Require an active account session for all financial API requests. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BearerTokenWebFilter implements WebFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/api/v1",
        "/api/v1/",
        "/api/v1/auth/signup",
        "/api/v1/auth/login",
        "/api/v1/auth/logout",
        "/api/v1/auth/me"
    );
    private final AccountService accounts;

    public BearerTokenWebFilter(AccountService accounts) {
        this.accounts = accounts;
    }

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        WebFilterChain chain
    ) {
        String path = exchange.getRequest().getPath().value();
        if (
            !path.startsWith("/api/") ||
            PUBLIC_PATHS.contains(path) ||
            HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())
        ) {
            return chain.filter(exchange);
        }
        exchange.getResponse().getHeaders().setCacheControl("no-store");
        var cookie = exchange
            .getRequest()
            .getCookies()
            .getFirst("expenselytics_session");
        if (cookie == null) return unauthorized(exchange);
        return Mono.fromCallable(() ->
            accounts.current(cookie.getValue())
        )
            .subscribeOn(Schedulers.boundedElastic())
            .map(profile -> true)
            .onErrorResume(AccountException.class, e -> Mono.just(false))
            .flatMap(valid ->
                valid ? chain.filter(exchange) : unauthorized(exchange)
            );
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        byte[] body = (
            "{\"title\":\"Unauthorized\",\"status\":401," +
            "\"detail\":\"Please log in to access your finances\"}"
        ).getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange
            .getResponse()
            .getHeaders()
            .setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        var buffer = exchange.getResponse().bufferFactory().wrap(body);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
