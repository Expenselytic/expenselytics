package com.expenlytics.web.controller;

import com.expenlytics.core.model.Expense;
import com.expenlytics.core.usecase.GetExpenseImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class GetExpenseController {
    public static final String GET_EXPENSE = ApiRootController.ROOT+"/getExpense";

    private final GetExpenseImpl getExpenseImpl;

    public GetExpenseController(GetExpenseImpl getExenseImpl) {
        this.getExpenseImpl = getExenseImpl;
    }


    @GetMapping(GET_EXPENSE)
    public Mono<List<Expense>> getAllExpenses() {
        return getExpenseImpl.getExpenses()
                .onErrorResume(ex -> {
                    return Mono.just(List.of()); // or Mono.error(new CustomException(...))
                });
    }
}
