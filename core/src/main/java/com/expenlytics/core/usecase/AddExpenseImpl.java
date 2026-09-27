package com.expenlytics.core.usecase;

import com.expenlytics.core.CreateExpense;
import com.expenlytics.core.dao.AddExpenseDAO;
import com.expenlytics.core.exception.InvalidInformationException;
import com.expenlytics.core.model.Expense;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@Lazy
public class AddExpenseImpl implements CreateExpense {
    public static final String AMOUNT = "^\\d+(\\.\\d{1,2})?$";

    private final AddExpenseDAO addExpenseDAO;

    public AddExpenseImpl(AddExpenseDAO addExpenseDAO) {
        this.addExpenseDAO = addExpenseDAO;
    }

    @Override
    public Mono<Expense> createExpense(Expense expense) {
        validateExpense(expense);
        return Mono.fromCallable(() -> addExpenseDAO.createExpense(expense))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void validateExpense(Expense expense) {
        if (expense == null) {
            throw new InvalidInformationException("Expense is required");
        }
        if (expense.getName() == null || expense.getName().isBlank()) {
            throw new InvalidInformationException("Name is required");
        }
        else if (expense.getCategory() == null || expense.getCategory().isBlank()) {
            throw new InvalidInformationException("Category is required");
        }
        else if (expense.getAmount() == null || expense.getAmount().isBlank()) {
            throw new InvalidInformationException("Amount is required");
        }
        else if(!expense.getAmount().trim().matches(AMOUNT)) {
            throw new InvalidInformationException("Amount must be a valid number");
        }
        else if (expense.getDate() == null) {
            throw new InvalidInformationException("Date is required");
        }
        else if (expense.getDate().isAfter(LocalDateTime.now())) {
            throw new InvalidInformationException("Date cannot be in the future");
        }
    }
}
