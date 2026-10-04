package com.expenlytics.core;

import com.expenlytics.core.model.Expense;
import java.util.List;
import reactor.core.publisher.Mono;

public interface GetExpenses {
    Mono<List<Expense>> getExpenses();
}
