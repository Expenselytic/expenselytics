package com.expenlytics.web.model;

import com.expenlytics.core.model.Expense;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Component
public class ExpenseModelAssembler {
    public ExpenseModel toModel(Expense expense) {
        String href = "/api/v1/expenses/" + expense.getId();
        return new ExpenseModel(expense)
                .add(Link.of(href).withSelfRel())
                .add(Link.of(href).withRel("edit"))
                .add(Link.of(href).withRel("delete"))
                .add(Link.of("/api/v1/expenses").withRel("collection"));
    }
}
