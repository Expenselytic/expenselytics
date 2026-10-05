package com.expenlytics.web.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.
    MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class BearerTokenWebFilterTest {

    private final BearerTokenWebFilter filter = new BearerTokenWebFilter(
        "test-token"
    );

    @Test
    void allowsBrowserSessionAndRejectsExpiredSession() {
        var accounts = org.mockito.Mockito.mock(com.expenlytics.core.usecase.AccountService.class);
        var sessionFilter = new BearerTokenWebFilter("test-token", accounts);
        org.mockito.Mockito.when(accounts.current("session")).thenReturn(
            new com.expenlytics.core.model.AccountProfile(1L, "Jane", "jane@example.com", java.time.Instant.now()));
        var request = MockServerHttpRequest.post("/api/v1/savings")
            .cookie(new org.springframework.http.HttpCookie("expenselytics_session", "session")).build();
        AtomicBoolean called = new AtomicBoolean();
        sessionFilter.filter(MockServerWebExchange.from(request), ignored -> {
            called.set(true); return Mono.empty();
        }).block();
        assertTrue(called.get());
        org.mockito.Mockito.when(accounts.current("session")).thenThrow(
            new com.expenlytics.core.exception.AccountException(
                com.expenlytics.core.exception.AccountException.Reason.UNAUTHORIZED, "Expired"));
        called.set(false);
        var expired = MockServerWebExchange.from(request);
        sessionFilter.filter(expired, ignored -> { called.set(true); return Mono.empty(); }).block();
        assertFalse(called.get());
        assertEquals(401, expired.getResponse().getStatusCode().value());
    }

    @Test
    void rejectsMutationWithoutToken() {
        var request = MockServerHttpRequest.method(
            HttpMethod.POST,
            "/api/v1/expenses"
        ).build();
        var exchange = MockServerWebExchange.from(request);
        AtomicBoolean called = new AtomicBoolean();

        filter
            .filter(exchange, ignored -> {
                called.set(true);
                return Mono.empty();
            })
            .block();

        assertEquals(401, exchange.getResponse().getStatusCode().value());
        assertFalse(called.get());
    }

    @Test
    void allowsMutationWithValidToken() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.method(
                HttpMethod.DELETE,
                "/api/v1/expenses/1"
            )
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .build()
        );
        AtomicBoolean called = new AtomicBoolean();

        filter
            .filter(exchange, ignored -> {
                called.set(true);
                return Mono.empty();
            })
            .block();

        assertTrue(called.get());
    }
}
