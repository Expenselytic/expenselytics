package com.expenlytics.db.impl;

import com.expenlytics.core.dao.AddExpenseDAO;
import com.expenlytics.db.entity.AddExpenseEntity;
import com.expenlytics.db.repository.ExpenseRepository;
import com.expenlytics.core.model.Expense;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
@Lazy
@Transactional
public class AddExpenseDaoImpl implements AddExpenseDAO {
    private final ExpenseRepository expenseRepository;

    public AddExpenseDaoImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Expense createExpense(Expense expense) {
        AddExpenseEntity entity = new AddExpenseEntity(expense);
        AddExpenseEntity saved = expenseRepository.save(entity);
        return new Expense(
                saved.getId(),
                saved.getName(),
                saved.getCategory(),
                saved.getAmount(),
                saved.getDate());
    }
}
