package com.expenlytics.web.controller;

import com.expenlytics.core.GetExpense;
import com.expenlytics.core.UpdateExpense;
import com.expenlytics.web.model.ExpenseModel;
import com.expenlytics.web.model.ExpenseModelAssembler;
import com.expenlytics.web.model.ExpenseRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ApiRootController.ROOT + "/expenses")
@CrossOrigin(
    origins = { "http://localhost:8080", "http://localhost:4200" }
)
public class ExpenseDetailsController {

    private final GetExpense getExpense;
    private final UpdateExpense updateExpense;
    private final ExpenseModelAssembler assembler;

    public ExpenseDetailsController(
        GetExpense getExpense,
        UpdateExpense updateExpense,
        ExpenseModelAssembler assembler
    ) {
        this.getExpense = getExpense;
        this.updateExpense = updateExpense;
        this.assembler = assembler;
    }

    @GetMapping("/{id}")
    public Mono<ExpenseModel> get(@PathVariable Long id) {
        return getExpense.getExpense(id).map(assembler::toModel);
    }

    @PutMapping("/{id}")
    public Mono<ExpenseModel> update(
        @PathVariable Long id,
        @RequestBody ExpenseRequest request
    ) {
        return updateExpense
            .updateExpense(id, request.toDomain())
            .map(assembler::toModel);
    }
}
