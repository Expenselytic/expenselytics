package com.expenlytics.core.dao;

import com.expenlytics.core.model.Expense;

public interface UpdateExpenseDAO {
    Expense updateExpense(Long id, Expense expense);
}
