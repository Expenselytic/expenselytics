package com.expenlytics.core;

import com.expenlytics.core.model.AddExpense;
import reactor.core.publisher.Mono;

public interface CreateExpense {
    Mono<String> createExpense(AddExpense addExpense);
}
