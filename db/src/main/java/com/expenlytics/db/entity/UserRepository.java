package com.expenlytics.db.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AddExpense, Long> {
}
