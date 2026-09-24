-- ====================================================================
-- VentureLens Database Schema for MySQL / Aiven Cloud
-- Strictly pure MySQL schema with foreign keys, indexes, and constraints
-- ====================================================================

CREATE DATABASE IF NOT EXISTS defaultdb;
USE defaultdb;

-- 1. Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS ventures;
DROP TABLE IF EXISTS users;

-- 2. Users Table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL, -- SHA-256 hex string (64 chars)
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Ventures & Idea Validations Table
CREATE TABLE ventures (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    startup_name VARCHAR(120) NOT NULL,
    target_customer VARCHAR(255) NOT NULL,
    problem_statement TEXT NOT NULL,
    proposed_solution TEXT NOT NULL,
    business_model VARCHAR(100) NOT NULL,
    
    -- Analysis Scores
    market_score DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    feasibility_score DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    competition_score DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    depth_score DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    overall_score DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    decision_tier ENUM('STRONG_GO', 'GO', 'CAUTION', 'PIVOT') NOT NULL DEFAULT 'CAUTION',
    
    -- Extracted SWOT & Risk Summary
    strengths_json TEXT,
    weaknesses_json TEXT,
    opportunities_json TEXT,
    threats_json TEXT,
    critical_risks_json TEXT,
    
    -- CapTable Snapshot
    founder_equity DECIMAL(5, 2) DEFAULT 80.00,
    esop_pool DECIMAL(5, 2) DEFAULT 10.00,
    investor_equity DECIMAL(5, 2) DEFAULT 10.00,
    pre_money_val DECIMAL(15, 2) DEFAULT 5000000.00,
    investment_amount DECIMAL(15, 2) DEFAULT 1000000.00,
    post_money_val DECIMAL(15, 2) DEFAULT 6000000.00,
    
    -- BurnWatch Snapshot
    current_cash_balance DECIMAL(15, 2) DEFAULT 500000.00,
    monthly_burn DECIMAL(15, 2) DEFAULT 35000.00,
    runway_months DECIMAL(5, 1) DEFAULT 14.3,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_venture_user FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    INDEX idx_venture_user (user_id),
    INDEX idx_venture_decision (decision_tier),
    INDEX idx_venture_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Expenses / Ledger Table for BurnWatch
CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venture_id INT NOT NULL,
    category ENUM('SALARY', 'HOSTING_INFRA', 'MARKETING', 'LEGAL_ADMIN', 'REVENUE', 'OTHER') NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL, -- Positive for expense, negative or flagged for revenue
    is_revenue BOOLEAN NOT NULL DEFAULT FALSE,
    month_year VARCHAR(7) NOT NULL, -- Format YYYY-MM
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_expense_venture FOREIGN KEY (venture_id) 
        REFERENCES ventures(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    INDEX idx_expense_venture (venture_id),
    INDEX idx_expense_month (month_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Seed Initial Demo User (Password: admin123 -> SHA-256)
-- SHA-256 for 'admin123': 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO users (id, username, email, password_hash, full_name) VALUES
(1, 'founder', 'founder@venturelens.ai', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Alex Vance');

-- 6. Seed Initial Demo Venture
INSERT INTO ventures (
    id, user_id, startup_name, target_customer, problem_statement, proposed_solution, business_model,
    market_score, feasibility_score, competition_score, depth_score, overall_score, decision_tier,
    strengths_json, weaknesses_json, opportunities_json, threats_json, critical_risks_json,
    founder_equity, esop_pool, investor_equity, pre_money_val, investment_amount, post_money_val,
    current_cash_balance, monthly_burn, runway_months
) VALUES (
    1, 1, 'OmniRoute Cloud', 'Mid-market Logistics Operators & Freight Carriers',
    'High diesel fuel costs and manual dispatch planning create 22% empty-mile waste without real-time telemetry.',
    'Automated dynamic routing and load matching algorithm using IoT edge GPS trackers and instant dispatch API.',
    'B2B SaaS Subscription ($499/mo per fleet) + 1.5% transaction fee on spot-market carrier load booking',
    84.50, 78.00, 72.00, 88.00, 81.10, 'STRONG_GO',
    'High market pain point, scalable B2B recurring revenue, edge computing defensibility',
    'Requires driver mobile app adoption and initial hardware telemetry onboarding',
    'Expanding ESG regulatory mandates for carbon reduction in commercial freight',
    'Incumbent TMS software vendors expanding into automated dispatch modules',
    'Long enterprise sales cycles and hardware driver supply chain bottlenecks',
    75.00, 10.00, 15.00, 8000000.00, 1500000.00, 9500000.00,
    1200000.00, 68000.00, 17.6
);

-- 7. Seed Initial Expenses for BurnWatch
INSERT INTO expenses (venture_id, category, description, amount, is_revenue, month_year) VALUES
(1, 'SALARY', 'Founding Engineering Team (3 Devs)', 28000.00, FALSE, '2026-03'),
(1, 'HOSTING_INFRA', 'Aiven Cloud MySQL + AWS Edge Fleet Cluster', 4200.00, FALSE, '2026-03'),
(1, 'MARKETING', 'Logistics Summit Booth & Search Ads', 6500.00, FALSE, '2026-03'),
(1, 'LEGAL_ADMIN', 'Delaware C-Corp & Patent Filing Retainer', 3500.00, FALSE, '2026-03'),
(1, 'REVENUE', 'Beta Fleet Pilot Subscriptions (6 Clients)', 12500.00, TRUE, '2026-03');
