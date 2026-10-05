package com.expenlytics.web.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.expenlytics.core.exception.AccountException;
import com.expenlytics.core.model.AccountProfile;
import com.expenlytics.core.usecase.AccountService;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.
    MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class BearerTokenWebFilterTest {

    private final AccountService accounts = mock(AccountService.class);
    private final BearerTokenWebFilter filter = new BearerTokenWebFilter(
        accounts
    );

    @Test
    void rejectsAnonymousFinancialReadsAndWrites() {
        for (String path : new String[] {
            "/api/v1/expenses",
            "/api/v1/savings",
            "/api/v1/experiments",
            "/api/v1/getExpense",
            "/api/v1/expenses/1",
        }) {
            for (HttpMethod method : new HttpMethod[] {
                HttpMethod.GET,
                HttpMethod.POST,
                HttpMethod.DELETE,
                HttpMethod.PUT,
                HttpMethod.HEAD,
            }) {
                var exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.method(method, path).build()
                );
                AtomicBoolean called = new AtomicBoolean();
                filter
                    .filter(exchange, ignored -> {
                        called.set(true);
                        return Mono.empty();
                    })
                    .block();
                assertFalse(called.get());
                assertEquals(
                    401,
                    exchange.getResponse().getStatusCode().value()
                );
                assertEquals(
                    "no-store",
                    exchange.getResponse().getHeaders().getCacheControl()
                );
            }
        }
    }

    @Test
    void allowsBrowserSessionAndRejectsExpiredSession() {
        when(accounts.current("session")).thenReturn(
            new AccountProfile(
                1L,
                "Jane",
                "jane@example.com",
                Instant.now()
            )
        );
        var request = MockServerHttpRequest.get("/api/v1/savings")
            .cookie(new HttpCookie("expenselytics_session", "session"))
            .build();
        AtomicBoolean called = new AtomicBoolean();
        filter
            .filter(MockServerWebExchange.from(request), ignored -> {
                called.set(true);
                return Mono.empty();
            })
            .block();
        assertTrue(called.get());
        when(accounts.current("session")).thenThrow(
            new AccountException(
                AccountException.Reason.UNAUTHORIZED,
                "Expired"
            )
        );
        called.set(false);
        var expired = MockServerWebExchange.from(request);
        filter
            .filter(expired, ignored -> {
                called.set(true);
                return Mono.empty();
            })
            .block();
        assertFalse(called.get());
        assertEquals(401, expired.getResponse().getStatusCode().value());
    }

    @Test
    void sharedBearerTokenCannotBypassLogin() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/expenses")
                .header("Authorization", "Bearer dev-only-token")
                .build()
        );
        filter
            .filter(exchange, ignored -> {
                fail("Bearer token must not bypass account login");
                return Mono.empty();
            })
            .block();
        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void permitsLoginAndPublicPages() {
        for (String path : new String[] {
            "/",
            "/login",
            "/workspace",
            "/api/v1",
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
        }) {
            AtomicBoolean called = new AtomicBoolean();
            var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(path).build()
            );
            filter
                .filter(exchange, ignored -> {
                    called.set(true);
                    return Mono.empty();
                })
                .block();
            assertTrue(called.get());
        }
    }
}
