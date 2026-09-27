package com.expenlytics.web.controller;

import com.expenlytics.core.usecase.GetExpenseImpl;
import com.expenlytics.web.model.ExpenseModel;
import com.expenlytics.web.model.ExpenseModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import reactor.core.publisher.Mono;


@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200"})
public class GetExpenseController {
    public static final String GET_EXPENSE = ApiRootController.ROOT+"/getExpense";

    private final GetExpenseImpl getExpenseImpl;
    private final ExpenseModelAssembler assembler;

    public GetExpenseController(
            GetExpenseImpl getExenseImpl,
            ExpenseModelAssembler assembler) {
        this.getExpenseImpl = getExenseImpl;
        this.assembler = assembler;
    }


    @GetMapping({GET_EXPENSE, ApiRootController.ROOT + "/expenses"})
    public Mono<CollectionModel<ExpenseModel>> getAllExpenses() {
        return getExpenseImpl.getExpenses().map(expenses -> CollectionModel.of(
                expenses.stream().map(assembler::toModel).toList(),
                Link.of(ApiRootController.ROOT + "/expenses").withSelfRel(),
                Link.of(ApiRootController.ROOT + "/expenses").withRel("addExpense")));
    }
}
