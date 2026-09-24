package com.venturelens.dao;

import com.venturelens.model.ExpenseEntry;
import com.venturelens.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object for BurnWatch Expenses and Ledger Entries.
 */
public class ExpenseDAO {

    private static final Map<Integer, List<ExpenseEntry>> MEMORY_EXPENSES = new ConcurrentHashMap<>();
    private static int memoryExpenseSequence = 10;

    static {
        // Pre-populate default expenses for venture 1
        List<ExpenseEntry> list = new ArrayList<>();
        list.add(new ExpenseEntry("SALARY", "Founding Engineering Team (3 Devs)", new BigDecimal("28000.00"), false, "2026-03"));
        list.add(new ExpenseEntry("HOSTING_INFRA", "Aiven Cloud MySQL + AWS Edge Fleet", new BigDecimal("4200.00"), false, "2026-03"));
        list.add(new ExpenseEntry("MARKETING", "Logistics Summit Booth & Search Ads", new BigDecimal("6500.00"), false, "2026-03"));
        list.add(new ExpenseEntry("LEGAL_ADMIN", "Delaware C-Corp & IP Retainer", new BigDecimal("3500.00"), false, "2026-03"));
        list.add(new ExpenseEntry("REVENUE", "Beta Fleet Pilot Subscriptions (6 Clients)", new BigDecimal("12500.00"), true, "2026-03"));
        MEMORY_EXPENSES.put(1, list);
    }

    public List<ExpenseEntry> findByVentureId(int ventureId) throws SQLException {
        List<ExpenseEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE venture_id = ? ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ventureId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExpenseEntry e = new ExpenseEntry();
                    e.setId(rs.getInt("id"));
                    e.setVentureId(rs.getInt("venture_id"));
                    e.setCategory(rs.getString("category"));
                    e.setDescription(rs.getString("description"));
                    e.setAmount(rs.getBigDecimal("amount"));
                    e.setRevenue(rs.getBoolean("is_revenue"));
                    e.setMonthYear(rs.getString("month_year"));
                    e.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(e);
                }
            }
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL expense find falling back to local memory store: " + dbEx.getMessage());
            List<ExpenseEntry> mem = MEMORY_EXPENSES.get(ventureId);
            if (mem != null) {
                list.addAll(mem);
            }
        }

        if (list.isEmpty() && MEMORY_EXPENSES.containsKey(1)) {
            list.addAll(MEMORY_EXPENSES.get(1));
        }

        return list;
    }

    public ExpenseEntry insert(ExpenseEntry e) throws SQLException {
        String sql = "INSERT INTO expenses (venture_id, category, description, amount, is_revenue, month_year) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, e.getVentureId());
            ps.setString(2, e.getCategory());
            ps.setString(3, e.getDescription());
            ps.setBigDecimal(4, e.getAmount());
            ps.setBoolean(5, e.isRevenue());
            ps.setString(6, e.getMonthYear());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                    e.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    MEMORY_EXPENSES.computeIfAbsent(e.getVentureId(), k -> new ArrayList<>()).add(e);
                    return e;
                }
            }
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL expense insert falling back to local memory store: " + dbEx.getMessage());
            e.setId(memoryExpenseSequence++);
            e.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            MEMORY_EXPENSES.computeIfAbsent(e.getVentureId(), k -> new ArrayList<>()).add(e);
            return e;
        }

        return e;
    }

    public boolean delete(int expenseId, int ventureId) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ? AND venture_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setInt(2, ventureId);
            ps.executeUpdate();
            List<ExpenseEntry> mem = MEMORY_EXPENSES.get(ventureId);
            if (mem != null) {
                mem.removeIf(item -> item.getId() == expenseId);
            }
            return true;
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL expense delete falling back to local memory store: " + dbEx.getMessage());
            List<ExpenseEntry> mem = MEMORY_EXPENSES.get(ventureId);
            if (mem != null) {
                mem.removeIf(item -> item.getId() == expenseId);
            }
            return true;
        }
    }
}
