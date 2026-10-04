package com.expenlytics.db.impl;

import com.expenlytics.core.dao.GetExpenseDAO;
import com.expenlytics.core.model.Expense;
import com.expenlytics.db.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
@Lazy
@Transactional
public class GetExpenseDaoImpl implements GetExpenseDAO {

    private final ExpenseRepository expenseRepository;

    public GetExpenseDaoImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public List<Expense> getExpense() {
        var entities = expenseRepository.findAll();

        return entities
            .stream()
            .map(entity ->
                new Expense(
                    entity.getId(),
                    entity.getName(),
                    entity.getCategory(),
                    entity.getAmount(),
                    entity.getDate()
                )
            )
            .toList();
    }
}
