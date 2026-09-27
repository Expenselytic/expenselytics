package com.expenlytics.web.model;

import com.expenlytics.core.model.Expense;

import java.time.LocalDateTime;

public record ExpenseRequest(
        String name,
        String category,
        String amount,
        LocalDateTime date) {
    public Expense toDomain() {
        return new Expense(0, name, category, amount, date);
    }
}
