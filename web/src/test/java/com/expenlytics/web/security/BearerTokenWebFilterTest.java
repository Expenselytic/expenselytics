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
