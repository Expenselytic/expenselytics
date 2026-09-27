package com.expenlytics.web.controller;

import com.expenlytics.core.CreateExpense;
import com.expenlytics.web.model.ExpenseModel;
import com.expenlytics.web.model.ExpenseModelAssembler;
import com.expenlytics.web.model.ExpenseRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200"})
public class AddExpenseController {
    public static final String ADD_EXPENSE = ApiRootController.ROOT+"/addExpense";
    private final CreateExpense createExpense;
    private final ExpenseModelAssembler assembler;

    public AddExpenseController(
            CreateExpense createExpense,
            ExpenseModelAssembler assembler) {
        this.createExpense = createExpense;
        this.assembler = assembler;
    }

    @PostMapping({ADD_EXPENSE, ApiRootController.ROOT + "/expenses"})
    public Mono<ResponseEntity<ExpenseModel>> addExpense(
            @RequestBody ExpenseRequest request) {
        return createExpense.createExpense(request.toDomain())
                .map(assembler::toModel)
                .map(model -> ResponseEntity
                        .created(model.getRequiredLink("self").toUri())
                        .body(model));
    }


}
