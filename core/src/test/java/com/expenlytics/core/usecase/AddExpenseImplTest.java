package com.expenlytics.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.expenlytics.core.exception.InvalidInformationException;
import com.expenlytics.core.model.Expense;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AddExpenseImplTest {

    @Test
    void createsValidExpense() {
        Expense saved = new Expense(
            12,
            "Lunch",
            "Food",
            "15.50",
            LocalDateTime.now().minusMinutes(1)
        );
        AddExpenseImpl useCase = new AddExpenseImpl(expense -> saved);

        assertEquals(12, useCase.createExpense(saved).block().getId());
    }

    @Test
    void rejectsInvalidAmount() {
        AddExpenseImpl useCase = new AddExpenseImpl(expense -> expense);
        Expense invalid = new Expense(
            0,
            "Lunch",
            "Food",
            "not-money",
            LocalDateTime.now().minusMinutes(1)
        );

        assertThrows(InvalidInformationException.class, () ->
            useCase.createExpense(invalid)
        );
    }
}
