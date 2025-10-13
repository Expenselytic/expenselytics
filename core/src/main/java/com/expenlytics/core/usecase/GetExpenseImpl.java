package com.expenlytics.core.usecase;

import com.expenlytics.core.GetExpenses;
import com.expenlytics.core.dao.GetExpenseDAO;
import com.expenlytics.core.model.Expense;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Lazy
public class GetExpenseImpl implements GetExpenses {
    public final GetExpenseDAO getExpenseDAO;

    public GetExpenseImpl(GetExpenseDAO getExpenseDAO) {
        this.getExpenseDAO = getExpenseDAO;
    }

    @Override
    public Mono<List<Expense>> getExpenses() {
        return Mono.fromCallable(getExpenseDAO::getExpense)
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex ->
                        Mono.error(new RuntimeException
                                ("Failed to fetch expenses", ex)));
    }
}
