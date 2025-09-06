package com.expenlytics.db.repository;

import com.expenlytics.db.entity.AddExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreateExpenseRepository extends JpaRepository<AddExpenseEntity, Long> {
}
