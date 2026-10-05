package com.expenlytics.db.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "monthly_budget", uniqueConstraints = @UniqueConstraint(
    columnNames = { "user_id", "month", "category" }
))
public class BudgetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(nullable = false, length = 7)
    public String month;

    // An empty category represents the overall monthly budget.
    @Column(nullable = false, length = 255)
    public String category;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal amount;
}
