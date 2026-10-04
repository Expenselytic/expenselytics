package com.expenlytics.core.usecase;

import com.expenlytics.core.DeleteExpense;
import com.expenlytics.core.dao.DeleteExpenseDAO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Lazy
public class DeleteExpenseImpl implements DeleteExpense {

    private final DeleteExpenseDAO deleteExpenseDAO;

    public DeleteExpenseImpl(DeleteExpenseDAO deleteExpenseDAO) {
        this.deleteExpenseDAO = deleteExpenseDAO;
    }

    public Mono<Void> deleteExpense(Long id) {
        this.validateId(id);
        return Mono.fromRunnable(() -> deleteExpenseDAO.deleteExpense(id))
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
    }
}
