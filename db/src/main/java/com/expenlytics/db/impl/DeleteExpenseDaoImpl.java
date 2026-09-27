package com.expenlytics.db.impl;

import com.expenlytics.core.dao.DeleteExpenseDAO;
import com.expenlytics.core.exception.NotFoundException;
import com.expenlytics.db.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
@Lazy
@Transactional
public class DeleteExpenseDaoImpl implements DeleteExpenseDAO {
    private final ExpenseRepository expenseRepository;

    public DeleteExpenseDaoImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
        throw new NotFoundException("Expense with ID " + id + " not found");
    }
        expenseRepository.deleteById(id);

    }

}
