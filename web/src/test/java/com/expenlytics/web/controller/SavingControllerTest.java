package com.expenlytics.web.controller;

import static org.mockito.Mockito.*;

import com.expenlytics.core.model.AccountProfile;
import com.expenlytics.core.usecase.AccountService;
import com.expenlytics.db.repository.SavingRepository;
import com.expenlytics.web.security.BearerTokenWebFilter;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class SavingControllerTest {

    private final SavingRepository savings = mock(SavingRepository.class);
    private final AccountService accounts = mock(AccountService.class);
    private final WebTestClient client = WebTestClient.bindToController(
        new SavingController(savings)
    )
        .webFilter(new BearerTokenWebFilter(accounts))
        .build();

    @Test
    void deletesSavingAndSupportsRepeatedDeletion() {
        when(accounts.current("session")).thenReturn(
            new AccountProfile(
                1L,
                "Jane",
                "jane@example.com",
                Instant.now()
            )
        );
        for (int i = 0; i < 2; i++) {
            client
                .delete()
                .uri("/api/v1/savings/7")
                .cookie("expenselytics_session", "session")
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .isEmpty();
        }
        verify(savings, times(2)).deleteById(7L);
    }

    @Test
    void rejectsUnauthenticatedDeletion() {
        client
            .delete()
            .uri("/api/v1/savings/7")
            .exchange()
            .expectStatus()
            .isUnauthorized();
        verifyNoInteractions(savings);
    }
}
