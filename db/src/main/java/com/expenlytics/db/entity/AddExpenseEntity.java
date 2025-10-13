package com.expenlytics.db.entity;

import com.expenlytics.core.model.Expense;
import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "expense")
public class AddExpenseEntity {
    @Column(nullable = false)
    private int uid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String amount;

    @Column(nullable = false)
    private LocalDateTime date;

    public AddExpenseEntity(){}

    public AddExpenseEntity(Expense expense) {
        uid = 1;
        name = expense.getName();
        category = expense.getCategory();
        amount = expense.getAmount();
        date = expense.getDate();
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getAmount() {
        return amount.trim();
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

}
