package com.expenlytics.repository;

import com.expenlytics.entity.AddExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreateExpenseRepository extends JpaRepository<AddExpenseEntity, Long> {
}
