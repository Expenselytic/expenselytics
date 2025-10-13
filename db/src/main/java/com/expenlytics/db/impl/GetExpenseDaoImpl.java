package com.expenlytics.db.impl;

import com.expenlytics.core.dao.GetExpenseDAO;
import com.expenlytics.core.model.Expense;
import com.expenlytics.db.repository.CreateExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@Lazy
@Transactional
public class GetExpenseDaoImpl implements GetExpenseDAO {
    private final CreateExpenseRepository createExpenseRepository;

    public GetExpenseDaoImpl(CreateExpenseRepository createExpenseRepository) {
        this.createExpenseRepository = createExpenseRepository;
    }

    @Override
    public List<Expense> getExpense() {
        var entities = createExpenseRepository.findAll();

        return entities.stream().map(entity -> new Expense(entity.getName(),
                entity.getCategory(),
                entity.getAmount(),
                entity.getDate())).toList();
    }
}
