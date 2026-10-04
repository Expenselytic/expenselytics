package com.expenlytics.web.controller;

import com.expenlytics.core.DeleteExpense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ApiRootController.ROOT)
@CrossOrigin(
    origins = { "http://localhost:8080", "http://localhost:4200" }
)
public class DeleteExpenseController {

    public static final String DELETE_EXPENSE =
        ApiRootController.ROOT + "/deleteExpense";

    private final DeleteExpense deleteExpense;

    public DeleteExpenseController(DeleteExpense deleteExpense) {
        this.deleteExpense = deleteExpense;
    }

    @DeleteMapping({ "/deleteExpense/{id}", "/expenses/{id}" })
    public Mono<Void> deleteExpense(@PathVariable Long id) {
        return deleteExpense.deleteExpense(id);
    }
}
