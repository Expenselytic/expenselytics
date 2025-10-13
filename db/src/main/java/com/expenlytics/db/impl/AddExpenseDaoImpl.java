package com.expenlytics.db.impl;

import com.expenlytics.core.dao.AddExpenseDAO;
import com.expenlytics.db.entity.AddExpenseEntity;
import com.expenlytics.db.repository.CreateExpenseRepository;
import com.expenlytics.core.model.Expense;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
@Lazy
@Transactional
public class AddExpenseDaoImpl implements AddExpenseDAO {
    private final CreateExpenseRepository createExpenseRepository;

    public AddExpenseDaoImpl(CreateExpenseRepository createExpenseRepository) {
        this.createExpenseRepository = createExpenseRepository;
    }

    @Override
    public void createExpense(Expense expense) {
        createExpenseRepository.save(new AddExpenseEntity(expense));
    }
}
