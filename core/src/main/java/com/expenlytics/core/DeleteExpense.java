package com.expenlytics.core;

import reactor.core.publisher.Mono;

public interface DeleteExpense {
    Mono<Void> deleteExpense(Long id);
}
