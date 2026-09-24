package com.venturelens.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Pure JDBC Connection Manager configured specifically for Aiven Cloud MySQL.
 * Strictly uses java.sql.* with try-with-resources.
 */
public class DatabaseConnection {

    private static String dbType = "postgresql";
    private static String host = "pg-24c49dac-gowthamgowri73-1585.h.aivencloud.com";
    private static int port = 28072;
    private static String database = "defaultdb";
    private static String username = "avnadmin";
    private static String password = "";
    private static String sslMode = "require"; // require, REQUIRED, VERIFY_CA, VERIFY_IDENTITY
    private static String sslCaPath = "";

    private static boolean driverRegistered = false;

    static {
        loadProperties();
    }

    public static void loadProperties() {
        Properties props = new Properties();
        try {
            File localFile = new File("db.properties");
            if (localFile.exists()) {
                try (InputStream in = new FileInputStream(localFile)) {
                    props.load(in);
                }
            } else {
                try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                    if (in != null) {
                        props.load(in);
                    }
                }
            }

            if (!props.isEmpty()) {
                dbType = props.getProperty("db.type", dbType);
                host = props.getProperty("db.host", host);
                try {
                    port = Integer.parseInt(props.getProperty("db.port", String.valueOf(port)));
                } catch (NumberFormatException ignored) {}
                database = props.getProperty("db.name", database);
                username = props.getProperty("db.user", username);
                password = props.getProperty("db.password", password);
                sslMode = props.getProperty("db.sslMode", sslMode);
                sslCaPath = props.getProperty("db.sslCaPath", sslCaPath);
            }
        } catch (Exception e) {
            System.err.println("Notice: using default or environment database settings: " + e.getMessage());
        }

        // Environment overrides
        if (System.getenv("AIVEN_PG_HOST") != null) host = System.getenv("AIVEN_PG_HOST");
        else if (System.getenv("AIVEN_MYSQL_HOST") != null) host = System.getenv("AIVEN_MYSQL_HOST");

        if (System.getenv("AIVEN_PG_PORT") != null) {
            try { port = Integer.parseInt(System.getenv("AIVEN_PG_PORT")); } catch (NumberFormatException ignored) {}
        } else if (System.getenv("AIVEN_MYSQL_PORT") != null) {
            try { port = Integer.parseInt(System.getenv("AIVEN_MYSQL_PORT")); } catch (NumberFormatException ignored) {}
        }

        if (System.getenv("AIVEN_PG_DB") != null) database = System.getenv("AIVEN_PG_DB");
        else if (System.getenv("AIVEN_MYSQL_DB") != null) database = System.getenv("AIVEN_MYSQL_DB");

        if (System.getenv("AIVEN_PG_USER") != null) username = System.getenv("AIVEN_PG_USER");
        else if (System.getenv("AIVEN_MYSQL_USER") != null) username = System.getenv("AIVEN_MYSQL_USER");

        if (System.getenv("AIVEN_PG_PASSWORD") != null) password = System.getenv("AIVEN_PG_PASSWORD");
        else if (System.getenv("AIVEN_MYSQL_PASSWORD") != null) password = System.getenv("AIVEN_MYSQL_PASSWORD");
    }

    public static String getJdbcUrl() {
        if (host != null && (host.startsWith("pg-") || "postgresql".equalsIgnoreCase(dbType) || port == 28072)) {
            // PostgreSQL JDBC URL
            return "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=" + (sslMode != null && !sslMode.isEmpty() ? sslMode : "require");
        }
        // MySQL JDBC URL
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://").append(host).append(":").append(port).append("/").append(database);
        url.append("?useSSL=true");
        url.append("&sslMode=").append(sslMode != null && !sslMode.isEmpty() ? sslMode : "REQUIRED");
        url.append("&allowPublicKeyRetrieval=true");
        url.append("&serverTimezone=UTC");
        url.append("&characterEncoding=UTF-8");
        url.append("&connectTimeout=8000");

        if (sslCaPath != null && !sslCaPath.trim().isEmpty()) {
            url.append("&sslTrustStoreType=PEM");
            url.append("&sslTrustStorePath=").append(sslCaPath.trim());
        }

        return url.toString();
    }

    private static synchronized void registerDriver() throws SQLException {
        if (!driverRegistered) {
            try {
                if (host != null && (host.startsWith("pg-") || "postgresql".equalsIgnoreCase(dbType) || port == 28072)) {
                    Class.forName("org.postgresql.Driver");
                } else {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                }
                driverRegistered = true;
            } catch (ClassNotFoundException e) {
                System.err.println("Notice: JDBC Driver not found in classpath. Proceeding with standard DriverManager discovery: " + e.getMessage());
            }
        }
    }

    /**
     * Gets a new live Connection to Aiven MySQL. Callers MUST close it via try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        registerDriver();
        return DriverManager.getConnection(getJdbcUrl(), username, password);
    }

    /**
     * Tests connectivity to Aiven MySQL cloud instance.
     * Returns latency in milliseconds.
     */
    public static long testConnection() throws SQLException {
        long start = System.currentTimeMillis();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
        }
        return System.currentTimeMillis() - start;
    }

    // Getters and Setters
    public static String getHost() { return host; }
    public static void setHost(String h) { host = h; }

    public static int getPort() { return port; }
    public static void setPort(int p) { port = p; }

    public static String getDatabase() { return database; }
    public static void setDatabase(String db) { database = db; }

    public static String getUsername() { return username; }
    public static void setUsername(String u) { username = u; }

    public static String getPassword() { return password; }
    public static void setPassword(String p) { password = p; }

    public static String getSslMode() { return sslMode; }
    public static void setSslMode(String m) { sslMode = m; }

    public static String getSslCaPath() { return sslCaPath; }
    public static void setSslCaPath(String p) { sslCaPath = p; }
}
