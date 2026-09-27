package com.expenlytics.core.usecase;

import com.expenlytics.core.UpdateExpense;
import com.expenlytics.core.dao.UpdateExpenseDAO;
import com.expenlytics.core.exception.InvalidInformationException;
import com.expenlytics.core.model.Expense;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
public class UpdateExpenseImpl implements UpdateExpense {
    private static final String AMOUNT = "^\\d+(\\.\\d{1,2})?$";
    private final UpdateExpenseDAO updateExpenseDAO;

    public UpdateExpenseImpl(UpdateExpenseDAO updateExpenseDAO) {
        this.updateExpenseDAO = updateExpenseDAO;
    }

    @Override
    public Mono<Expense> updateExpense(Long id, Expense expense) {
        validate(id, expense);
        return Mono.fromCallable(() -> updateExpenseDAO.updateExpense(id, expense))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void validate(Long id, Expense expense) {
        if (id == null || id <= 0) {
            throw new InvalidInformationException("ID must be positive");
        }
        if (expense == null) {
            throw new InvalidInformationException("Expense is required");
        }
        if (expense.getName() == null || expense.getName().isBlank()) {
            throw new InvalidInformationException("Name is required");
        }
        if (expense.getCategory() == null
                || expense.getCategory().isBlank()) {
            throw new InvalidInformationException("Category is required");
        }
        if (expense.getAmount() == null
                || !expense.getAmount().trim().matches(AMOUNT)) {
            throw new InvalidInformationException(
                    "Amount must be a valid number");
        }
        if (expense.getDate() == null) {
            throw new InvalidInformationException("Date is required");
        }
        if (expense.getDate().isAfter(LocalDateTime.now())) {
            throw new InvalidInformationException(
                    "Date cannot be in the future");
        }
    }
}
