package com.expenlytics.core;

import com.expenlytics.core.model.Expense;
import reactor.core.publisher.Mono;

public interface UpdateExpense {
    Mono<Expense> updateExpense(Long id, Expense expense);
}
