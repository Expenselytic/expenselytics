package com.expenlytics.db.model;

import com.expenlytics.core.model.Expense;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class ExpenseTest {
    @Test
    public void testAddExpense() {
        // Create an instance of AddExpense
        Expense expense = new Expense("Lunch", "Food", "15.50", LocalDateTime.now());

        // Validate the properties
        assert "Lunch".equals(expense.getName());
        assert "Food".equals(expense.getCategory());
        assert "15.50".equals(expense.getAmount());
        assert expense.getDate() != null;

        // Modify the properties
        expense.setName("Dinner");
        expense.setCategory("Dining");
        expense.setAmount("20.00");
        expense.setDate(LocalDateTime.now().plusDays(1));

        // Validate the modified properties
        assert "Dinner".equals(expense.getName());
        assert "Dining".equals(expense.getCategory());
        assert "20.00".equals(expense.getAmount());
        assert expense.getDate().isAfter(LocalDateTime.now());
    }
}
