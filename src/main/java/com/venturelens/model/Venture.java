package com.venturelens.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class representing a complete Venture evaluation, CapTable state, and financial snapshot.
 */
public class Venture {
    private int id;
    private int userId;
    private String startupName;
    private String targetCustomer;
    private String problemStatement;
    private String proposedSolution;
    private String businessModel;

    // Scores
    private BigDecimal marketScore;
    private BigDecimal feasibilityScore;
    private BigDecimal competitionScore;
    private BigDecimal depthScore;
    private BigDecimal overallScore;
    private String decisionTier; // STRONG_GO, GO, CAUTION, PIVOT

    // SWOT & Risks
    private String strengths;
    private String weaknesses;
    private String opportunities;
    private String threats;
    private String criticalRisks;

    // CapTable
    private BigDecimal founderEquity;
    private BigDecimal esopPool;
    private BigDecimal investorEquity;
    private BigDecimal preMoneyValuation;
    private BigDecimal investmentAmount;
    private BigDecimal postMoneyValuation;

    // BurnWatch
    private BigDecimal currentCashBalance;
    private BigDecimal monthlyBurn;
    private BigDecimal runwayMonths;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Venture() {
        this.marketScore = BigDecimal.ZERO;
        this.feasibilityScore = BigDecimal.ZERO;
        this.competitionScore = BigDecimal.ZERO;
        this.depthScore = BigDecimal.ZERO;
        this.overallScore = BigDecimal.ZERO;
        this.decisionTier = "CAUTION";
        this.founderEquity = new BigDecimal("80.00");
        this.esopPool = new BigDecimal("10.00");
        this.investorEquity = new BigDecimal("10.00");
        this.preMoneyValuation = new BigDecimal("5000000.00");
        this.investmentAmount = new BigDecimal("1000000.00");
        this.postMoneyValuation = new BigDecimal("6000000.00");
        this.currentCashBalance = new BigDecimal("500000.00");
        this.monthlyBurn = new BigDecimal("35000.00");
        this.runwayMonths = new BigDecimal("14.3");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getStartupName() { return startupName != null ? startupName : ""; }
    public void setStartupName(String startupName) { this.startupName = startupName; }

    public String getTargetCustomer() { return targetCustomer != null ? targetCustomer : ""; }
    public void setTargetCustomer(String targetCustomer) { this.targetCustomer = targetCustomer; }

    public String getProblemStatement() { return problemStatement != null ? problemStatement : ""; }
    public void setProblemStatement(String problemStatement) { this.problemStatement = problemStatement; }

    public String getProposedSolution() { return proposedSolution != null ? proposedSolution : ""; }
    public void setProposedSolution(String proposedSolution) { this.proposedSolution = proposedSolution; }

    public String getBusinessModel() { return businessModel != null ? businessModel : ""; }
    public void setBusinessModel(String businessModel) { this.businessModel = businessModel; }

    public BigDecimal getMarketScore() { return marketScore != null ? marketScore : BigDecimal.ZERO; }
    public void setMarketScore(BigDecimal marketScore) { this.marketScore = marketScore; }

    public BigDecimal getFeasibilityScore() { return feasibilityScore != null ? feasibilityScore : BigDecimal.ZERO; }
    public void setFeasibilityScore(BigDecimal feasibilityScore) { this.feasibilityScore = feasibilityScore; }

    public BigDecimal getCompetitionScore() { return competitionScore != null ? competitionScore : BigDecimal.ZERO; }
    public void setCompetitionScore(BigDecimal competitionScore) { this.competitionScore = competitionScore; }

    public BigDecimal getDepthScore() { return depthScore != null ? depthScore : BigDecimal.ZERO; }
    public void setDepthScore(BigDecimal depthScore) { this.depthScore = depthScore; }

    public BigDecimal getOverallScore() { return overallScore != null ? overallScore : BigDecimal.ZERO; }
    public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }

    public String getDecisionTier() { return decisionTier != null ? decisionTier : "CAUTION"; }
    public void setDecisionTier(String decisionTier) { this.decisionTier = decisionTier; }

    public String getStrengths() { return strengths != null ? strengths : ""; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses != null ? weaknesses : ""; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }

    public String getOpportunities() { return opportunities != null ? opportunities : ""; }
    public void setOpportunities(String opportunities) { this.opportunities = opportunities; }

    public String getThreats() { return threats != null ? threats : ""; }
    public void setThreats(String threats) { this.threats = threats; }

    public String getCriticalRisks() { return criticalRisks != null ? criticalRisks : ""; }
    public void setCriticalRisks(String criticalRisks) { this.criticalRisks = criticalRisks; }

    public BigDecimal getFounderEquity() { return founderEquity != null ? founderEquity : BigDecimal.ZERO; }
    public void setFounderEquity(BigDecimal founderEquity) { this.founderEquity = founderEquity; }

    public BigDecimal getEsopPool() { return esopPool != null ? esopPool : BigDecimal.ZERO; }
    public void setEsopPool(BigDecimal esopPool) { this.esopPool = esopPool; }

    public BigDecimal getInvestorEquity() { return investorEquity != null ? investorEquity : BigDecimal.ZERO; }
    public void setInvestorEquity(BigDecimal investorEquity) { this.investorEquity = investorEquity; }

    public BigDecimal getPreMoneyValuation() { return preMoneyValuation != null ? preMoneyValuation : BigDecimal.ZERO; }
    public void setPreMoneyValuation(BigDecimal preMoneyValuation) { this.preMoneyValuation = preMoneyValuation; }

    public BigDecimal getInvestmentAmount() { return investmentAmount != null ? investmentAmount : BigDecimal.ZERO; }
    public void setInvestmentAmount(BigDecimal investmentAmount) { this.investmentAmount = investmentAmount; }

    public BigDecimal getPostMoneyValuation() { return postMoneyValuation != null ? postMoneyValuation : BigDecimal.ZERO; }
    public void setPostMoneyValuation(BigDecimal postMoneyValuation) { this.postMoneyValuation = postMoneyValuation; }

    public BigDecimal getCurrentCashBalance() { return currentCashBalance != null ? currentCashBalance : BigDecimal.ZERO; }
    public void setCurrentCashBalance(BigDecimal currentCashBalance) { this.currentCashBalance = currentCashBalance; }

    public BigDecimal getMonthlyBurn() { return monthlyBurn != null ? monthlyBurn : BigDecimal.ZERO; }
    public void setMonthlyBurn(BigDecimal monthlyBurn) { this.monthlyBurn = monthlyBurn; }

    public BigDecimal getRunwayMonths() { return runwayMonths != null ? runwayMonths : BigDecimal.ZERO; }
    public void setRunwayMonths(BigDecimal runwayMonths) { this.runwayMonths = runwayMonths; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return startupName + " (" + decisionTier + " - " + overallScore + ")";
    }
}
