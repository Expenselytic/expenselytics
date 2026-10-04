package com.expenlytics.db.impl;

import com.expenlytics.core.dao.FindExpenseDAO;
import com.expenlytics.core.exception.NotFoundException;
import com.expenlytics.core.model.Expense;
import com.expenlytics.db.entity.AddExpenseEntity;
import com.expenlytics.db.repository.ExpenseRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class FindExpenseDaoImpl implements FindExpenseDAO {

    private final ExpenseRepository repository;

    public FindExpenseDaoImpl(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Expense findExpense(Long id) {
        AddExpenseEntity entity = repository
            .findById(id)
            .orElseThrow(() ->
                new NotFoundException(
                    "Expense with ID " + id + " not found"
                )
            );
        return new Expense(
            entity.getId(),
            entity.getName(),
            entity.getCategory(),
            entity.getAmount(),
            entity.getDate()
        );
    }
}
