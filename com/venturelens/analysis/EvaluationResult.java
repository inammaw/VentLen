package com.venturelens.analysis;

import java.util.List;

public class EvaluationResult {
    private final String startupName;
    private final double marketScore;
    private final double feasibilityScore;
    private final double competitionScore;
    private final double conceptDepthScore;
    private final double overallScore;
    private final DecisionTier tier;
    private final List<String> strengths;
    private final List<String> weaknesses;
    private final List<String> opportunities;
    private final List<String> threats;
    private final List<String> criticalRisks;

    public EvaluationResult(
            String startupName,
            double marketScore,
            double feasibilityScore,
            double competitionScore,
            double conceptDepthScore,
            double overallScore,
            DecisionTier tier,
            List<String> strengths,
            List<String> weaknesses,
            List<String> opportunities,
            List<String> threats,
            List<String> criticalRisks) {
        this.startupName = startupName;
        this.marketScore = marketScore;
        this.feasibilityScore = feasibilityScore;
        this.competitionScore = competitionScore;
        this.conceptDepthScore = conceptDepthScore;
        this.overallScore = overallScore;
        this.tier = tier;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.opportunities = opportunities;
        this.threats = threats;
        this.criticalRisks = criticalRisks;
    }

    public String getStartupName() {
        return startupName;
    }

    public double getMarketScore() {
        return marketScore;
    }

    public double getFeasibilityScore() {
        return feasibilityScore;
    }

    public double getCompetitionScore() {
        return competitionScore;
    }

    public double getConceptDepthScore() {
        return conceptDepthScore;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public DecisionTier getTier() {
        return tier;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public List<String> getOpportunities() {
        return opportunities;
    }

    public List<String> getThreats() {
        return threats;
    }

    public List<String> getCriticalRisks() {
        return criticalRisks;
    }
}
