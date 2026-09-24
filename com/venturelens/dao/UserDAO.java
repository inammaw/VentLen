package com.venturelens.dao;

import com.venturelens.model.User;
import com.venturelens.utils.DatabaseConnection;
import com.venturelens.utils.SecurityUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object for User entities.
 * Strictly uses java.sql.* with try-with-resources.
 * Includes local memory fallback if live MySQL network is not configured.
 */
public class UserDAO {

    // In-memory fallback repository for offline/local resilience
    private static final Map<String, User> MEMORY_USERS = new ConcurrentHashMap<>();
    private static int memoryIdSequence = 2;

    static {
        // Pre-populate demo user 'founder' (password: admin123)
        User demo = new User(1, "founder", "founder@venturelens.ai",
                SecurityUtils.hashPassword("admin123"), "Alex Vance");
        MEMORY_USERS.put("founder", demo);
    }

    /**
     * Registers a new user.
     */
    public User registerUser(String username, String email, String plainPassword, String fullName) throws SQLException {
        String hashedPassword = SecurityUtils.hashPassword(plainPassword);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (username, email, password_hash, full_name) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);
            ps.setString(4, fullName);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    User user = new User(id, username, email, hashedPassword, fullName);
                    MEMORY_USERS.put(username, user);
                    return user;
                }
            }
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL registration falling back to local memory store: " + dbEx.getMessage());
            // Fallback
            if (MEMORY_USERS.containsKey(username)) {
                throw new SQLException("Username already exists in system");
            }
            int id = memoryIdSequence++;
            User user = new User(id, username, email, hashedPassword, fullName);
            MEMORY_USERS.put(username, user);
            return user;
        }

        throw new SQLException("Failed to create user account");
    }

    /**
     * Authenticates a user by username and password.
     */
    public User authenticate(String username, String plainPassword) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, username, email, password_hash, full_name, created_at FROM users WHERE username = ?")) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (SecurityUtils.verifyPassword(plainPassword, storedHash)) {
                        User user = new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                storedHash,
                                rs.getString("full_name")
                        );
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        return user;
                    } else {
                        return null; // Invalid password
                    }
                }
            }
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL authentication falling back to local memory store: " + dbEx.getMessage());
            // Local fallback
            User user = MEMORY_USERS.get(username);
            if (user != null && SecurityUtils.verifyPassword(plainPassword, user.getPasswordHash())) {
                return user;
            }
            return null;
        }

        return null; // User not found
    }
}
