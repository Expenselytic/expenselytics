package com.expenlytics.core.model;

import java.time.LocalDateTime;

public class Expense {

    private long id;
    private String name;
    private String category;
    private String amount;
    private LocalDateTime date;

    public Expense() {}

    public Expense(
        long id,
        String name,
        String category,
        String amount,
        LocalDateTime date
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public long getUid() {
        return this.id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getAmount() {
        return amount == null ? null : amount.trim();
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
