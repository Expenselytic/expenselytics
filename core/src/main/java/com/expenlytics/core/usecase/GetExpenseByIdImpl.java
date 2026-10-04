package com.expenlytics.core.usecase;

import com.expenlytics.core.GetExpense;
import com.expenlytics.core.dao.FindExpenseDAO;
import com.expenlytics.core.exception.InvalidInformationException;
import com.expenlytics.core.model.Expense;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class GetExpenseByIdImpl implements GetExpense {

    private final FindExpenseDAO findExpenseDAO;

    public GetExpenseByIdImpl(FindExpenseDAO findExpenseDAO) {
        this.findExpenseDAO = findExpenseDAO;
    }

    @Override
    public Mono<Expense> getExpense(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidInformationException("ID must be positive");
        }
        return Mono.fromCallable(() ->
            findExpenseDAO.findExpense(id)
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
