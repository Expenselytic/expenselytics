package com.expenlytics.core.dao;

import com.expenlytics.core.model.Expense;

public interface FindExpenseDAO {
    Expense findExpense(Long id);
}
