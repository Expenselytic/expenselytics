package com.expenlytics;

import com.expenlytics.model.AddExpense;
import reactor.core.publisher.Mono;

public interface CreateExpense {
    Mono<String> createExpense(AddExpense addExpense);
}
