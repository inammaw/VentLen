package com.venturelens.analysis;

import com.venturelens.model.Venture;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Scoring and SWOT categorization engine.
 * Computes weighted composite validation score and decision tiers.
 */
public class ScoringEngine {

    public static class EvaluationOutput {
        public BigDecimal marketScore;
        public BigDecimal feasibilityScore;
        public BigDecimal competitionScore;
        public BigDecimal depthScore;
        public BigDecimal overallScore;
        public String decisionTier; // STRONG_GO, GO, CAUTION, PIVOT

        public String strengths;
        public String weaknesses;
        public String opportunities;
        public String threats;
        public String criticalRisks;
    }

    /**
     * Evaluates a venture's problem, solution, customer, and business model using StartupEvaluator.
     * 100% deterministic, rule-based algorithmic evaluation with zero AI dependency.
     */
    public static EvaluationOutput evaluate(String name, String customer, String problem, String solution, String model) {
        EvaluationOutput out = new EvaluationOutput();

        EvaluationResult res = StartupEvaluator.evaluate(name, customer, problem, solution, model);

        out.marketScore = BigDecimal.valueOf(res.getMarketScore()).setScale(2, RoundingMode.HALF_UP);
        out.feasibilityScore = BigDecimal.valueOf(res.getFeasibilityScore()).setScale(2, RoundingMode.HALF_UP);
        out.competitionScore = BigDecimal.valueOf(res.getCompetitionScore()).setScale(2, RoundingMode.HALF_UP);
        out.depthScore = BigDecimal.valueOf(res.getConceptDepthScore()).setScale(2, RoundingMode.HALF_UP);
        out.overallScore = BigDecimal.valueOf(res.getOverallScore()).setScale(2, RoundingMode.HALF_UP);
        out.decisionTier = res.getTier().name();

        out.strengths = res.getStrengths().isEmpty()
                ? "Early-stage concept with clear foundational problem scope."
                : String.join(" • ", res.getStrengths());

        out.weaknesses = res.getWeaknesses().isEmpty()
                ? "Early customer validation required to confirm unit economics."
                : String.join(" • ", res.getWeaknesses());

        out.opportunities = res.getOpportunities().isEmpty()
                ? "Expansion into adjacent market verticals and customer segments."
                : String.join(" • ", res.getOpportunities());

        out.threats = res.getThreats().isEmpty()
                ? "Competitive alternatives and macro-budget headwinds."
                : String.join(" • ", res.getThreats());

        out.criticalRisks = res.getCriticalRisks().isEmpty()
                ? "Execution risk: Building repeatable go-to-market and sustaining runway."
                : String.join("; ", res.getCriticalRisks());

        return out;
    }

    /**
     * Applies evaluation output directly to a Venture entity.
     */
    public static void applyToVenture(Venture v, EvaluationOutput out) {
        v.setMarketScore(out.marketScore);
        v.setFeasibilityScore(out.feasibilityScore);
        v.setCompetitionScore(out.competitionScore);
        v.setDepthScore(out.depthScore);
        v.setOverallScore(out.overallScore);
        v.setDecisionTier(out.decisionTier);
        v.setStrengths(out.strengths);
        v.setWeaknesses(out.weaknesses);
        v.setOpportunities(out.opportunities);
        v.setThreats(out.threats);
        v.setCriticalRisks(out.criticalRisks);
    }
}
