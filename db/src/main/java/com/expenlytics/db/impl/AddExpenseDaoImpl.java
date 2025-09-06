package com.expenlytics.impl;

import com.expenlytics.dao.AddExpenseDAO;
import com.expenlytics.entity.AddExpenseEntity;
import com.expenlytics.repository.CreateExpenseRepository;
import com.expenlytics.model.AddExpense;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
@Lazy
@Transactional
public class AddExpenseDaoImpl implements AddExpenseDAO {
    @Autowired
    private CreateExpenseRepository createExpenseRepository;

    @Override
    public void createExpense(AddExpense addExpense) {
        createExpenseRepository.save(new AddExpenseEntity(addExpense));
    }
}
