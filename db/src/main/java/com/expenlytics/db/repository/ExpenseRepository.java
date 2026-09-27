package com.expenlytics.db.repository;

import com.expenlytics.db.entity.AddExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<AddExpenseEntity, Long> {
}
