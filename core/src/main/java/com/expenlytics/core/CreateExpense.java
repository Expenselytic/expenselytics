package com.expenlytics.core;

import com.expenlytics.core.model.Expense;
import reactor.core.publisher.Mono;

public interface CreateExpense {
    Mono<Expense> createExpense(Expense expense);
}
