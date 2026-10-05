package com.expenlytics.db.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "spending_experiment")
public class SpendingExperimentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(nullable = false, length = 255)
    public String category;

    @Column(nullable = false)
    public LocalDate startDate;

    @Column(nullable = false)
    public int durationDays;
}
