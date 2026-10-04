package com.expenlytics.core.usecase;

import com.expenlytics.core.GetExpenses;
import com.expenlytics.core.dao.GetExpenseDAO;
import com.expenlytics.core.model.Expense;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Lazy
public class GetExpenseImpl implements GetExpenses {

    public final GetExpenseDAO getExpenseDAO;

    public GetExpenseImpl(GetExpenseDAO getExpenseDAO) {
        this.getExpenseDAO = getExpenseDAO;
    }

    @Override
    public Mono<List<Expense>> getExpenses() {
        return Mono.fromCallable(getExpenseDAO::getExpense).subscribeOn(
            Schedulers.boundedElastic()
        );
    }
}
