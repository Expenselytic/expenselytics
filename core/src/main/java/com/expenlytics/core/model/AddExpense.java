package com.expenlytics.core.model;

import java.time.LocalDateTime;

public class AddExpense {
    private String name;
    private String category;
    private String amount;
    private LocalDateTime date;

    public AddExpense(String name, String category, String amount, LocalDateTime date) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.date = date;
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
