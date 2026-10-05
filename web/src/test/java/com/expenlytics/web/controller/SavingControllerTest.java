package com.expenlytics.web.controller;

import com.expenlytics.db.repository.SavingRepository;
import com.expenlytics.web.security.BearerTokenWebFilter;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.Mockito.*;

class SavingControllerTest {
    private final SavingRepository savings = mock(SavingRepository.class);
    private final WebTestClient client = WebTestClient.bindToController(new SavingController(savings))
        .webFilter(new BearerTokenWebFilter("test-token")).build();

    @Test
    void deletesSavingAndSupportsRepeatedDeletion() {
        for (int i = 0; i < 2; i++) {
            client.delete().uri("/api/v1/savings/7")
                .header("Authorization", "Bearer test-token")
                .exchange().expectStatus().isNoContent().expectBody().isEmpty();
        }
        verify(savings, times(2)).deleteById(7L);
    }

    @Test
    void rejectsUnauthenticatedDeletion() {
        client.delete().uri("/api/v1/savings/7").exchange().expectStatus().isUnauthorized();
        verifyNoInteractions(savings);
    }
}
