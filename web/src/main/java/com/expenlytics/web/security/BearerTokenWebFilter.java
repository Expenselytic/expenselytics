package com.expenlytics.web.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BearerTokenWebFilter implements WebFilter {

    private static final Set<HttpMethod> PROTECTED_METHODS = Set.of(
        HttpMethod.POST,
        HttpMethod.PUT,
        HttpMethod.PATCH,
        HttpMethod.DELETE
    );
    private final byte[] expectedToken;

    public BearerTokenWebFilter(
        @Value(
            "${app.security.auth-token:dev-only-token}"
        ) String expectedToken
    ) {
        this.expectedToken = expectedToken.getBytes(
            StandardCharsets.UTF_8
        );
    }

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        WebFilterChain chain
    ) {
        if (
            Set.of(
                "/api/v1/auth/signup",
                "/api/v1/auth/login",
                "/api/v1/auth/logout",
                "/api/v1/auth/me"
            ).contains(exchange.getRequest().getPath().value()) ||
            HttpMethod.OPTIONS.equals(
                exchange.getRequest().getMethod()
            ) ||
            !PROTECTED_METHODS.contains(
                exchange.getRequest().getMethod()
            ) ||
            !exchange.getRequest().getPath().value().startsWith("/api/")
        ) {
            return chain.filter(exchange);
        }

        String authorization = exchange
            .getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);
        String supplied =
            authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : "";
        byte[] suppliedToken = supplied.getBytes(StandardCharsets.UTF_8);
        if (MessageDigest.isEqual(expectedToken, suppliedToken)) {
            return chain.filter(exchange);
        }

        byte[] body = (
            "{\"title\":\"Unauthorized\",\"status\":401," +
            "\"detail\":\"A valid bearer token is required\"}"
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
