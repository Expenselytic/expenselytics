package com.expenlytics.web.controller;

import com.expenlytics.core.dao.AddExpenseDAO;
import com.expenlytics.core.exception.InvalidInformationException;
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
public class AddExpense {
    @Autowired
    private AddExpenseImpl addExpense;

   com.expenlytics.core.model.AddExpense addExpenses = new com.expenlytics.core.model.AddExpense("Lunch", "15.50", "Food", LocalDateTime.now());

    @PostMapping("/addExpense")
    public Mono<String> addExpense(@RequestBody com.expenlytics.core.model.AddExpense addExpenseRequest) {
        return Mono.just(addExpenseRequest)
                .flatMap(addExpense::createExpense) // call reactive service
                .onErrorMap(InvalidInformationException.class, e -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, e.getMessage()));
    }


}
