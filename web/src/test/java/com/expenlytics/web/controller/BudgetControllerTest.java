package com.expenlytics.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.expenlytics.core.model.AccountProfile;
import com.expenlytics.core.usecase.AccountService;
import com.expenlytics.db.entity.BudgetEntity;
import com.expenlytics.db.repository.BudgetRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class BudgetControllerTest {
    private final BudgetRepository repository =
        mock(BudgetRepository.class);
    private final AccountService accounts = mock(AccountService.class);
    private final BudgetController controller =
        new BudgetController(repository, accounts);

    @BeforeEach
    void authenticate() {
        when(accounts.current("session")).thenReturn(
            new AccountProfile(7L, "Jane", "jane@example.com",
                Instant.now())
        );
    }

    @Test
    void readsOnlyCurrentAccountsMonth() {
        when(repository.findByUserIdAndMonth(7L, "2026-10"))
            .thenReturn(List.of());
        assertEquals(List.of(),
            controller.list("2026-10", "session").block());
        verify(repository).findByUserIdAndMonth(7L, "2026-10");
    }

    @Test
    void updatesExistingCategoryWithoutCreatingDuplicate() {
        var existing = new BudgetEntity();
        existing.id = 5L;
        when(repository.findByUserIdAndMonthAndCategory(
            7L, "2026-10", "Home"
        )).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(call ->
            call.getArgument(0));
        var result = controller.save("2026-10",
            new BudgetController.Request(" Home ",
                new BigDecimal("125.50")), "session").block();
        assertEquals(5L, result.id);
        assertEquals(7L, result.userId);
        assertEquals("Home", result.category);
        assertEquals(new BigDecimal("125.50"), result.amount);
    }

    @Test
    void rejectsInvalidAmountsAndMonths() {
        for (String amount : List.of("0", "-1", "1.001")) {
            assertThrows(ResponseStatusException.class, () ->
                controller.save("2026-10",
                    new BudgetController.Request("",
                        new BigDecimal(amount)), "session").block());
        }
        assertThrows(ResponseStatusException.class, () ->
            controller.list("2026-13", "session").block());
        verifyNoInteractions(repository);
    }

    @Test
    void cannotDeleteAnotherAccountsBudget() {
        var budget = new BudgetEntity();
        budget.userId = 8L;
        when(repository.findById(1L)).thenReturn(Optional.of(budget));
        var error = assertThrows(ResponseStatusException.class, () ->
            controller.delete(1L, "session").block());
        assertEquals(404, error.getStatusCode().value());
        verify(repository, never()).delete(any(BudgetEntity.class));
    }
}
