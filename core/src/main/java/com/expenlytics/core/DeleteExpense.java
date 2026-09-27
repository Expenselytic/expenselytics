package com.expenlytics.core.usecase;

import reactor.core.publisher.Mono;

public interface DeleteExpense {
    Mono<Long> deleteExpense(Long id);
}
