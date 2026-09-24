package com.venturelens.dao;

import com.venturelens.model.Venture;
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
 * Data Access Object for Venture evaluations and CapTable states.
 * Strictly uses java.sql.* with try-with-resources.
 */
public class VentureDAO {

    private static final Map<Integer, Venture> MEMORY_VENTURES = new ConcurrentHashMap<>();
    private static int memoryVentureSequence = 2;

    static {
        // Seed default initial venture
        Venture v = new Venture();
        v.setId(1);
        v.setUserId(1);
        v.setStartupName("OmniRoute Cloud");
        v.setTargetCustomer("Mid-market Logistics Operators & Freight Carriers");
        v.setProblemStatement("High diesel fuel costs and manual dispatch planning create 22% empty-mile waste without real-time telemetry.");
        v.setProposedSolution("Automated dynamic routing and load matching algorithm using IoT edge GPS trackers and instant dispatch API.");
        v.setBusinessModel("B2B SaaS Subscription (₹39,999/mo per fleet) + 1.5% transaction fee on spot-market carrier load booking");
        v.setMarketScore(new BigDecimal("84.50"));
        v.setFeasibilityScore(new BigDecimal("78.00"));
        v.setCompetitionScore(new BigDecimal("72.00"));
        v.setDepthScore(new BigDecimal("88.00"));
        v.setOverallScore(new BigDecimal("81.10"));
        v.setDecisionTier("STRONG_GO");
        v.setStrengths("High market pain point • Scalable B2B recurring revenue • Edge computing defensibility");
        v.setWeaknesses("Requires driver mobile app adoption and initial hardware telemetry onboarding");
        v.setOpportunities("Expanding ESG regulatory mandates for carbon reduction in commercial freight");
        v.setThreats("Incumbent TMS software vendors expanding into automated dispatch modules");
        v.setCriticalRisks("Long enterprise sales cycles and hardware driver supply chain bottlenecks");
        v.setFounderEquity(new BigDecimal("75.00"));
        v.setEsopPool(new BigDecimal("10.00"));
        v.setInvestorEquity(new BigDecimal("15.00"));
        v.setPreMoneyValuation(new BigDecimal("8000000.00"));
        v.setInvestmentAmount(new BigDecimal("1500000.00"));
        v.setPostMoneyValuation(new BigDecimal("9500000.00"));
        v.setCurrentCashBalance(new BigDecimal("1200000.00"));
        v.setMonthlyBurn(new BigDecimal("68000.00"));
        v.setRunwayMonths(new BigDecimal("17.6"));
        v.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        MEMORY_VENTURES.put(1, v);
    }

    /**
     * Saves or updates a Venture in MySQL.
     */
    public Venture save(Venture v) throws SQLException {
        if (v.getId() > 0) {
            return update(v);
        }

        String sql = "INSERT INTO ventures (user_id, startup_name, target_customer, problem_statement, " +
                "proposed_solution, business_model, market_score, feasibility_score, competition_score, " +
                "depth_score, overall_score, decision_tier, strengths_json, weaknesses_json, opportunities_json, " +
                "threats_json, critical_risks_json, founder_equity, esop_pool, investor_equity, pre_money_val, " +
                "investment_amount, post_money_val, current_cash_balance, monthly_burn, runway_months) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setStatementParams(ps, v);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getInt(1));
                    v.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    MEMORY_VENTURES.put(v.getId(), v);
                    return v;
                }
            }
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL save falling back to local memory store: " + dbEx.getMessage());
            v.setId(memoryVentureSequence++);
            v.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            MEMORY_VENTURES.put(v.getId(), v);
            return v;
        }

        return v;
    }

    public Venture update(Venture v) throws SQLException {
        String sql = "UPDATE ventures SET startup_name=?, target_customer=?, problem_statement=?, proposed_solution=?, " +
                "business_model=?, market_score=?, feasibility_score=?, competition_score=?, depth_score=?, overall_score=?, " +
                "decision_tier=?, strengths_json=?, weaknesses_json=?, opportunities_json=?, threats_json=?, critical_risks_json=?, " +
                "founder_equity=?, esop_pool=?, investor_equity=?, pre_money_val=?, investment_amount=?, post_money_val=?, " +
                "current_cash_balance=?, monthly_burn=?, runway_months=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, v.getStartupName());
            ps.setString(idx++, v.getTargetCustomer());
            ps.setString(idx++, v.getProblemStatement());
            ps.setString(idx++, v.getProposedSolution());
            ps.setString(idx++, v.getBusinessModel());
            ps.setBigDecimal(idx++, v.getMarketScore());
            ps.setBigDecimal(idx++, v.getFeasibilityScore());
            ps.setBigDecimal(idx++, v.getCompetitionScore());
            ps.setBigDecimal(idx++, v.getDepthScore());
            ps.setBigDecimal(idx++, v.getOverallScore());
            ps.setString(idx++, v.getDecisionTier());
            ps.setString(idx++, v.getStrengths());
            ps.setString(idx++, v.getWeaknesses());
            ps.setString(idx++, v.getOpportunities());
            ps.setString(idx++, v.getThreats());
            ps.setString(idx++, v.getCriticalRisks());
            ps.setBigDecimal(idx++, v.getFounderEquity());
            ps.setBigDecimal(idx++, v.getEsopPool());
            ps.setBigDecimal(idx++, v.getInvestorEquity());
            ps.setBigDecimal(idx++, v.getPreMoneyValuation());
            ps.setBigDecimal(idx++, v.getInvestmentAmount());
            ps.setBigDecimal(idx++, v.getPostMoneyValuation());
            ps.setBigDecimal(idx++, v.getCurrentCashBalance());
            ps.setBigDecimal(idx++, v.getMonthlyBurn());
            ps.setBigDecimal(idx++, v.getRunwayMonths());
            ps.setInt(idx++, v.getId());

            ps.executeUpdate();
            MEMORY_VENTURES.put(v.getId(), v);
            return v;
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL update falling back to local memory store: " + dbEx.getMessage());
            MEMORY_VENTURES.put(v.getId(), v);
            return v;
        }
    }

    public List<Venture> findAllByUserId(int userId) throws SQLException {
        List<Venture> list = new ArrayList<>();
        String sql = "SELECT * FROM ventures WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVenture(rs));
                }
            }
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL find falling back to local memory store: " + dbEx.getMessage());
            for (Venture v : MEMORY_VENTURES.values()) {
                if (v.getUserId() == userId || userId == 1) {
                    list.add(v);
                }
            }
        }

        if (list.isEmpty() && !MEMORY_VENTURES.isEmpty()) {
            list.addAll(MEMORY_VENTURES.values());
        }

        return list;
    }

    public boolean deleteById(int ventureId) throws SQLException {
        String sql = "DELETE FROM ventures WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ventureId);
            ps.executeUpdate();
            MEMORY_VENTURES.remove(ventureId);
            return true;
        } catch (Exception dbEx) {
            System.err.println("Notice: MySQL delete falling back to local memory store: " + dbEx.getMessage());
            MEMORY_VENTURES.remove(ventureId);
            return true;
        }
    }

    private void setStatementParams(PreparedStatement ps, Venture v) throws SQLException {
        int idx = 1;
        ps.setInt(idx++, v.getUserId());
        ps.setString(idx++, v.getStartupName());
        ps.setString(idx++, v.getTargetCustomer());
        ps.setString(idx++, v.getProblemStatement());
        ps.setString(idx++, v.getProposedSolution());
        ps.setString(idx++, v.getBusinessModel());
        ps.setBigDecimal(idx++, v.getMarketScore());
        ps.setBigDecimal(idx++, v.getFeasibilityScore());
        ps.setBigDecimal(idx++, v.getCompetitionScore());
        ps.setBigDecimal(idx++, v.getDepthScore());
        ps.setBigDecimal(idx++, v.getOverallScore());
        ps.setString(idx++, v.getDecisionTier());
        ps.setString(idx++, v.getStrengths());
        ps.setString(idx++, v.getWeaknesses());
        ps.setString(idx++, v.getOpportunities());
        ps.setString(idx++, v.getThreats());
        ps.setString(idx++, v.getCriticalRisks());
        ps.setBigDecimal(idx++, v.getFounderEquity());
        ps.setBigDecimal(idx++, v.getEsopPool());
        ps.setBigDecimal(idx++, v.getInvestorEquity());
        ps.setBigDecimal(idx++, v.getPreMoneyValuation());
        ps.setBigDecimal(idx++, v.getInvestmentAmount());
        ps.setBigDecimal(idx++, v.getPostMoneyValuation());
        ps.setBigDecimal(idx++, v.getCurrentCashBalance());
        ps.setBigDecimal(idx++, v.getMonthlyBurn());
        ps.setBigDecimal(idx++, v.getRunwayMonths());
    }

    private Venture mapResultSetToVenture(ResultSet rs) throws SQLException {
        Venture v = new Venture();
        v.setId(rs.getInt("id"));
        v.setUserId(rs.getInt("user_id"));
        v.setStartupName(rs.getString("startup_name"));
        v.setTargetCustomer(rs.getString("target_customer"));
        v.setProblemStatement(rs.getString("problem_statement"));
        v.setProposedSolution(rs.getString("proposed_solution"));
        v.setBusinessModel(rs.getString("business_model"));
        v.setMarketScore(rs.getBigDecimal("market_score"));
        v.setFeasibilityScore(rs.getBigDecimal("feasibility_score"));
        v.setCompetitionScore(rs.getBigDecimal("competition_score"));
        v.setDepthScore(rs.getBigDecimal("depth_score"));
        v.setOverallScore(rs.getBigDecimal("overall_score"));
        v.setDecisionTier(rs.getString("decision_tier"));
        v.setStrengths(rs.getString("strengths_json"));
        v.setWeaknesses(rs.getString("weaknesses_json"));
        v.setOpportunities(rs.getString("opportunities_json"));
        v.setThreats(rs.getString("threats_json"));
        v.setCriticalRisks(rs.getString("critical_risks_json"));
        v.setFounderEquity(rs.getBigDecimal("founder_equity"));
        v.setEsopPool(rs.getBigDecimal("esop_pool"));
        v.setInvestorEquity(rs.getBigDecimal("investor_equity"));
        v.setPreMoneyValuation(rs.getBigDecimal("pre_money_val"));
        v.setInvestmentAmount(rs.getBigDecimal("investment_amount"));
        v.setPostMoneyValuation(rs.getBigDecimal("post_money_val"));
        v.setCurrentCashBalance(rs.getBigDecimal("current_cash_balance"));
        v.setMonthlyBurn(rs.getBigDecimal("monthly_burn"));
        v.setRunwayMonths(rs.getBigDecimal("runway_months"));
        v.setCreatedAt(rs.getTimestamp("created_at"));
        v.setUpdatedAt(rs.getTimestamp("updated_at"));
        return v;
    }
}
