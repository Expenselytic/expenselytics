package com.expenlytics.core.dao;

import com.expenlytics.core.model.Expense;

public interface AddExpenseDAO {
    Expense createExpense(Expense expense);
}
