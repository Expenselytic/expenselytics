package com.expenlytics.db.repository;

import com.expenlytics.db.entity.BudgetEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository
    extends JpaRepository<BudgetEntity, Long> {
    List<BudgetEntity> findByUserIdAndMonth(Long userId, String month);
    Optional<BudgetEntity> findByUserIdAndMonthAndCategory(
        Long userId, String month, String category
    );
}
