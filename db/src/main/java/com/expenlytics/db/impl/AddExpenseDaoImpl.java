package com.expenlytics.db.impl;

import com.expenlytics.core.dao.AddExpenseDAO;
import com.expenlytics.db.entity.AddExpenseEntity;
import com.expenlytics.db.repository.CreateExpenseRepository;
import com.expenlytics.core.model.AddExpense;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void createExpense(AddExpense addExpense) {
        createExpenseRepository.save(new AddExpenseEntity(addExpense));
    }
}
