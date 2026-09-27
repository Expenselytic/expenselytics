package com.expenlytics.db.impl;

import com.expenlytics.core.dao.UpdateExpenseDAO;
import com.expenlytics.core.exception.NotFoundException;
import com.expenlytics.core.model.Expense;
import com.expenlytics.db.entity.AddExpenseEntity;
import com.expenlytics.db.repository.ExpenseRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UpdateExpenseDaoImpl implements UpdateExpenseDAO {
    private final ExpenseRepository repository;

    public UpdateExpenseDaoImpl(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Expense updateExpense(Long id, Expense expense) {
        AddExpenseEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Expense with ID " + id + " not found"));
        entity.updateFrom(expense);
        AddExpenseEntity saved = repository.save(entity);
        return new Expense(
                saved.getId(),
                saved.getName(),
                saved.getCategory(),
                saved.getAmount(),
                saved.getDate());
    }
}
