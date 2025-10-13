package com.expenlytics.web.controller;

import com.expenlytics.web.model.ApiRootModel;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = ApiRootController.ROOT)
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200"})
public class ApiRootController {
    public static final String ROOT = "/api/v1";

    @GetMapping()
    public Mono<ApiRootModel> get() {
        return Mono.fromSupplier(this::toApiRootModel);
    }

    private ApiRootModel toApiRootModel() {
        var root = new ApiRootModel("1.0", "OK");
        return root
                .add(Link.of(ROOT))
                .add(Link.of(AddExpenseController.ADD_EXPENSE, "addExpense"))
                .add(Link.of(GetExpenseController.GET_EXPENSE, "getExpense"));
    }
}


