-- ====================================================================
-- VentureLens Database Schema for PostgreSQL / Aiven Cloud
-- Ready to run directly in Aiven Web Console -> Query Editor or psql
-- ====================================================================

-- 1. Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS expenses CASCADE;
DROP TABLE IF EXISTS ventures CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS decision_tier_enum CASCADE;
DROP TYPE IF EXISTS expense_category_enum CASCADE;

-- 2. Custom Enum Types
CREATE TYPE decision_tier_enum AS ENUM ('STRONG_GO', 'GO', 'CAUTION', 'PIVOT');
CREATE TYPE expense_category_enum AS ENUM ('SALARY', 'HOSTING_INFRA', 'MARKETING', 'LEGAL_ADMIN', 'REVENUE', 'OTHER');

-- 3. Users Table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);

-- 4. Ventures & Idea Validations Table
CREATE TABLE ventures (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    startup_name VARCHAR(120) NOT NULL,
    target_customer VARCHAR(255) NOT NULL,
    problem_statement TEXT NOT NULL,
    proposed_solution TEXT NOT NULL,
    business_model VARCHAR(150) NOT NULL,
    
    -- Analysis Scores
    market_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    feasibility_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    competition_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    depth_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    overall_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    decision_tier decision_tier_enum NOT NULL DEFAULT 'CAUTION',
    
    -- Strategic Analysis & Vectors
    strengths_json TEXT,
    weaknesses_json TEXT,
    opportunities_json TEXT,
    threats_json TEXT,
    critical_risks_json TEXT,
    moat_archetype VARCHAR(255),
    monetization_profile VARCHAR(255),
    
    -- CapTable Snapshot
    founder_equity NUMERIC(5, 2) DEFAULT 80.00,
    esop_pool NUMERIC(5, 2) DEFAULT 10.00,
    investor_equity NUMERIC(5, 2) DEFAULT 10.00,
    pre_money_val NUMERIC(15, 2) DEFAULT 5000000.00,
    investment_amount NUMERIC(15, 2) DEFAULT 1000000.00,
    post_money_val NUMERIC(15, 2) DEFAULT 6000000.00,
    
    -- BurnWatch Snapshot
    current_cash_balance NUMERIC(15, 2) DEFAULT 500000.00,
    monthly_burn NUMERIC(15, 2) DEFAULT 35000.00,
    runway_months NUMERIC(5, 1) DEFAULT 14.3,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_venture_user ON ventures(user_id);
CREATE INDEX idx_venture_decision ON ventures(decision_tier);
CREATE INDEX idx_venture_created ON ventures(created_at);

-- 5. Expenses / Ledger Table for BurnWatch
CREATE TABLE expenses (
    id SERIAL PRIMARY KEY,
    venture_id INT NOT NULL REFERENCES ventures(id) ON DELETE CASCADE ON UPDATE CASCADE,
    category expense_category_enum NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    is_revenue BOOLEAN NOT NULL DEFAULT FALSE,
    month_year VARCHAR(7) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expense_venture ON expenses(venture_id);
CREATE INDEX idx_expense_month ON expenses(month_year);

-- 6. Seed Users
INSERT INTO users (id, username, email, password_hash, full_name) VALUES
(1, 'gowtham', 'gowthams20070308@gmail.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Gowtham S');

-- 7. Seed Ventures (QuickDrop Logistics & OmniRoute Cloud)
INSERT INTO ventures (
    id, user_id, startup_name, target_customer, problem_statement, proposed_solution, business_model,
    market_score, feasibility_score, competition_score, depth_score, overall_score, decision_tier,
    strengths_json, weaknesses_json, opportunities_json, threats_json, critical_risks_json,
    moat_archetype, monetization_profile,
    founder_equity, esop_pool, investor_equity, pre_money_val, investment_amount, post_money_val,
    current_cash_balance, monthly_burn, runway_months
) VALUES 
(
    1, 1, 'QuickDrop Logistics', 
    'Small & Medium E-commerce Sellers in Regional Indian Cities',
    'Manual shipping coordination and fragmented local couriers lead to high logistics costs, delayed deliveries, and zero real-time package tracking.',
    'Automated multi-carrier dispatch orchestration engine with smart rate optimization and instant customer WhatsApp alerts.',
    'B2B subscription fee (₹1,999/month per merchant) + ₹4 convenience fee per dispatched parcel.',
    82.00, 85.00, 74.00, 80.00, 81.20, 'STRONG_GO',
    'High market pain point, scalable B2B recurring revenue, edge computing defensibility',
    'Requires regional delivery partner onboarding and driver application adoption',
    'Expanding tier-2 and tier-3 quick commerce and D2C online retail in India',
    'Legacy courier aggregation platforms offering discounted bulk shipping rates',
    'Cash-on-delivery reconciliation delays and regional courier operational strikes',
    'Automated Dispatch Orchestration & High Switching Cost',
    'B2B SaaS (₹1,999/mo) + ₹4/order Margin Fee (Est. 74% Gross Margin)',
    75.00, 10.00, 15.00, 10000000.00, 2000000.00, 12000000.00,
    1850000.00, 115000.00, 16.1
),
(
    2, 1, 'OmniRoute Cloud', 
    'Mid-market Logistics Operators & Commercial Freight Carriers',
    'High diesel fuel costs and manual dispatch planning create 22% empty-mile waste without real-time telemetry.',
    'Dynamic routing and load matching algorithm using IoT edge GPS trackers and dispatch API.',
    'B2B SaaS Subscription (₹39,999/mo per fleet) + 1.5% transaction fee on spot loads.',
    84.50, 78.00, 72.00, 88.00, 81.10, 'STRONG_GO',
    'Proven algorithmic fuel optimization, high recurring ACV, sticky fleet telematics',
    'Hardware dependency for legacy freight trucks without modern OBD ports',
    'Stricter carbon emissions monitoring standards across commercial transport corridors',
    'Incumbent TMS software vendors bundling basic GPS routing modules',
    'Extended enterprise sales cycle lengths exceeding 6 months',
    'Real-Time Telemetry & Fleet Network Effects',
    'Annual Enterprise SaaS Contract (₹4.8L ARR) + Transaction Fee',
    70.00, 15.00, 15.00, 25000000.00, 5000000.00, 30000000.00,
    3800000.00, 240000.00, 15.8
);

-- Adjust sequence counter for auto-incrementing serial IDs
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('ventures_id_seq', (SELECT MAX(id) FROM ventures));

-- 8. Seed Expenses for BurnWatch Ledger
INSERT INTO expenses (venture_id, category, description, amount, is_revenue, month_year) VALUES
(1, 'SALARY', 'Founding Engineering & Product (3 Members)', 65000.00, FALSE, '2026-03'),
(1, 'HOSTING_INFRA', 'Aiven Cloud PostgreSQL + Cloud Run Cluster', 12000.00, FALSE, '2026-03'),
(1, 'MARKETING', 'D2C Merchant WhatsApp Community & LinkedIn Ads', 18000.00, FALSE, '2026-03'),
(1, 'LEGAL_ADMIN', 'GST Compliance & Courier SLA Legal Drafting', 8000.00, FALSE, '2026-03'),
(1, 'REVENUE', 'Active Merchant Subscriptions (48 Sellers)', 96000.00, TRUE, '2026-03'),
(2, 'SALARY', 'Senior Systems & Embedded IoT Devs (4 Devs)', 140000.00, FALSE, '2026-03'),
(2, 'HOSTING_INFRA', 'AWS IoT Core & PostgreSQL Cloud Infrastructure', 25000.00, FALSE, '2026-03'),
(2, 'MARKETING', 'Freight Logistics Expo & Direct Enterprise Sales', 35000.00, FALSE, '2026-03'),
(2, 'REVENUE', 'Pilot Fleet Enterprise Contracts (3 Carriers)', 120000.00, TRUE, '2026-03');

SELECT setval('expenses_id_seq', (SELECT MAX(id) FROM expenses));
