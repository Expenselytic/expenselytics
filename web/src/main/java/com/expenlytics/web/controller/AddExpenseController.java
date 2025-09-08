package com.expenlytics.web.controller;

import com.expenlytics.core.exception.InvalidInformationException;
import com.expenlytics.core.model.AddExpense;
import com.expenlytics.core.usecase.AddExpenseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
public class AddExpenseController {
    public static final String ADD_EXPENSE = ApiRootController.ROOT+"/addExpense";
    @Autowired
    private AddExpenseImpl addExpense;

    @PostMapping(ADD_EXPENSE)
    public Mono<String> addExpense(@RequestBody AddExpense addExpenseRequest) {
        return Mono.just(addExpenseRequest)
                .flatMap(addExpense::createExpense) // call reactive service
                .onErrorMap(InvalidInformationException.class, e -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, e.getMessage()));
    }


}
