package com.venturelens.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class representing a monthly ledger entry for BurnWatch.
 */
public class ExpenseEntry {
    private int id;
    private int ventureId;
    private String category; // SALARY, HOSTING_INFRA, MARKETING, LEGAL_ADMIN, REVENUE, OTHER
    private String description;
    private BigDecimal amount;
    private boolean isRevenue;
    private String monthYear; // YYYY-MM
    private Timestamp createdAt;

    public ExpenseEntry() {
        this.amount = BigDecimal.ZERO;
        this.isRevenue = false;
    }

    public ExpenseEntry(String category, String description, BigDecimal amount, boolean isRevenue, String monthYear) {
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.isRevenue = isRevenue;
        this.monthYear = monthYear;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVentureId() { return ventureId; }
    public void setVentureId(int ventureId) { this.ventureId = ventureId; }

    public String getCategory() { return category != null ? category : "OTHER"; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount != null ? amount : BigDecimal.ZERO; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public boolean isRevenue() { return isRevenue; }
    public void setRevenue(boolean revenue) { isRevenue = revenue; }

    public String getMonthYear() { return monthYear != null ? monthYear : "2026-03"; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
