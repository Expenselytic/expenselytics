package com.expenlytics.web.model;

import com.expenlytics.core.model.Expense;
import java.time.LocalDateTime;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(itemRelation = "expense", collectionRelation = "expenses")
public class ExpenseModel extends RepresentationModel<ExpenseModel> {

    public final long id;
    public final String name;
    public final String category;
    public final String amount;
    public final LocalDateTime date;

    public ExpenseModel(Expense expense) {
        id = expense.getId();
        name = expense.getName();
        category = expense.getCategory();
        amount = expense.getAmount();
        date = expense.getDate();
    }
}
