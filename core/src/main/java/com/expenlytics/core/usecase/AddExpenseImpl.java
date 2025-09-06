package com.expenlytics.core.usecase;

import com.expenlytics.core.CreateExpense;
import com.expenlytics.core.dao.AddExpenseDAO;
import com.expenlytics.core.exception.InvalidInformationException;
import com.expenlytics.core.model.AddExpense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@Lazy
public class AddExpenseImpl implements CreateExpense {
    public static final String AMOUNT = "^\\d+(\\.\\d{1,2})?$";

    @Autowired
    private AddExpenseDAO addExpenseDAO;

    @Override
    public Mono<String> createExpense(AddExpense addExpense) {
        validateExpense(addExpense);
        try {
            return Mono.fromCallable(()->{
                addExpenseDAO.createExpense(addExpense);
                return "Expense created successfully";
            }).subscribeOn(Schedulers.boundedElastic());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validateExpense(AddExpense addExpense) {
        if (addExpense.getName().isEmpty()) {
            throw new InvalidInformationException("Name cannot be null");
        }
        else if (addExpense.getCategory().isEmpty()) {
            throw new InvalidInformationException("Category cannot be null");
        }
        else if (addExpense.getAmount().isEmpty()) {
            throw new InvalidInformationException("Amount cannot be null");
        }
        else if(AMOUNT.matches(addExpense.getAmount().trim())) {
            throw new InvalidInformationException("Amount must be a valid number");
        }
        else if (addExpense.getDate().isAfter(LocalDateTime.now())) {
            throw new InvalidInformationException("Date cannot be in the future");
        }
        else if (addExpense.getDate() == null) {
            throw new InvalidInformationException("Date cannot be null");
        }
    }
}
