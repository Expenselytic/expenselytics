package com.expenlytics.core;

import com.expenlytics.core.model.Expense;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GetExpenses {
    Mono<List<Expense>> getExpenses();
}
