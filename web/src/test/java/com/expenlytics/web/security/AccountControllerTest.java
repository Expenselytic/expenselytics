package com.expenlytics.web.security;

import static org.mockito.Mockito.*;

import com.expenlytics.core.exception.AccountException;
import com.expenlytics.core.model.*;
import com.expenlytics.core.usecase.AccountService;
import com.expenlytics.web.controller.AccountController;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

class AccountControllerTest {

    private final AccountService accounts = mock(
        AccountService.class,
        withSettings().mockMaker(org.mockito.MockMakers.SUBCLASS)
    );
    private final DeviceInfoCollector devices = mock(
        DeviceInfoCollector.class,
        withSettings().mockMaker(org.mockito.MockMakers.SUBCLASS)
    );
    private final WebTestClient client = WebTestClient.bindToController(
        new AccountController(
            accounts,
            devices,
            true,
            reactor.core.scheduler.Schedulers.immediate()
        )
    )
        .webFilter(new BearerTokenWebFilter("admin-token"))
        .build();

    @Test
    void signupIsPublicAndSetsSecurePrivateSessionCookie() {
        var profile = new AccountProfile(
            1L,
            "Jane",
            "jane@example.com",
            Instant.now()
        );
        when(devices.collect(any(), eq("SIGNUP"))).thenReturn(
            new DeviceInfo(
                null,
                null,
                "UNKNOWN",
                null,
                null,
                null,
                "SIGNUP"
            )
        );
        when(accounts.authenticate(any(), eq(true), any())).thenReturn(
            new AccountService.LoginResult("random-token", profile)
        );
        client
            .post()
            .uri("/api/v1/auth/signup")
            .bodyValue(
                new AccountCredentials(
                    "Jane",
                    "jane@example.com",
                    "long-password-123"
                )
            )
            .exchange()
            .expectStatus()
            .isCreated()
            .expectHeader()
            .valueEquals(HttpHeaders.CACHE_CONTROL, "no-store")
            .expectCookie()
            .path("expenselytics_session", "/api/v1")
            .expectCookie()
            .httpOnly("expenselytics_session", true)
            .expectCookie()
            .secure("expenselytics_session", true)
            .expectBody()
            .jsonPath("$.userId")
            .isEqualTo(1)
            .jsonPath("$.token")
            .doesNotExist()
            .jsonPath("$.passwordHash")
            .doesNotExist();
    }

    @Test
    void profileRequiresSessionCookieEvenWithAdminBearerToken() {
        client
            .get()
            .uri("/api/v1/auth/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
            .exchange()
            .expectStatus()
            .isUnauthorized();
        verifyNoInteractions(accounts);
    }

    @Test
    void logoutRevokesSessionAndExpiresCookie() {
        client
            .post()
            .uri("/api/v1/auth/logout")
            .cookie("expenselytics_session", "session")
            .exchange()
            .expectStatus()
            .isNoContent()
            .expectCookie()
            .maxAge("expenselytics_session", java.time.Duration.ZERO);
        verify(accounts).logout("session");
    }

    @Test
    void saturatedExecutorReturnsServiceUnavailable() {
        var scheduler =
            reactor.core.scheduler.Schedulers.newBoundedElastic(
                1,
                1,
                "test-auth"
            );
        scheduler.dispose();
        var overloaded = WebTestClient.bindToController(
            new AccountController(accounts, devices, true, scheduler)
        ).build();
        overloaded
            .get()
            .uri("/api/v1/auth/me")
            .cookie("expenselytics_session", "session")
            .exchange()
            .expectStatus()
            .isEqualTo(503);
        verifyNoInteractions(accounts);
    }

    @Test
    void invalidCredentialsMapToUnauthorized() {
        when(devices.collect(any(), eq("LOGIN"))).thenReturn(
            new DeviceInfo(
                null,
                null,
                "UNKNOWN",
                null,
                null,
                null,
                "LOGIN"
            )
        );
        when(accounts.authenticate(any(), eq(false), any())).thenThrow(
            new com.expenlytics.core.exception.AccountException(
                AccountException.Reason.UNAUTHORIZED,
                "Invalid email or password"
            )
        );
        client
            .post()
            .uri("/api/v1/auth/login")
            .bodyValue(
                new AccountCredentials(null, "jane@example.com", "wrong")
            )
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectCookie()
            .doesNotExist("expenselytics_session");
    }
}
