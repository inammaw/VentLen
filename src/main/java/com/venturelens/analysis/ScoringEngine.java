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
     * Evaluates a venture's problem, solution, customer, and business model using rule-based NLP.
     */
    public static EvaluationOutput evaluate(String name, String customer, String problem, String solution, String model) {
        EvaluationOutput out = new EvaluationOutput();

        String combinedText = (customer + " " + problem + " " + solution + " " + model).trim();
        RuleBasedNlpEngine.NlpResult nlp = RuleBasedNlpEngine.analyze(combinedText);

        // 1. Market Score (0-100)
        double marketBase = Math.min(100.0, 45.0 + (nlp.marketMatchCount * 9.0) - (nlp.negatedPhrases.size() * 5.0));
        if (customer != null && customer.length() > 15) marketBase += 10.0;
        out.marketScore = BigDecimal.valueOf(Math.max(15.0, Math.min(98.0, marketBase))).setScale(2, RoundingMode.HALF_UP);

        // 2. Feasibility Score (0-100)
        double feasBase = Math.min(100.0, 40.0 + (nlp.feasibilityMatchCount * 8.5));
        if (solution != null && solution.length() > 30) feasBase += 15.0;
        out.feasibilityScore = BigDecimal.valueOf(Math.max(20.0, Math.min(96.0, feasBase))).setScale(2, RoundingMode.HALF_UP);

        // 3. Competition Score (0-100)
        double compBase = Math.min(100.0, 50.0 + (nlp.competitionMatchCount * 11.0));
        out.competitionScore = BigDecimal.valueOf(Math.max(25.0, Math.min(95.0, compBase))).setScale(2, RoundingMode.HALF_UP);

        // 4. Concept Depth Score (0-100)
        int tokenCount = nlp.filteredTokens.size();
        double depthBase = Math.min(100.0, 30.0 + (tokenCount * 1.5));
        if (model != null && model.length() > 20) depthBase += 15.0;
        out.depthScore = BigDecimal.valueOf(Math.max(20.0, Math.min(98.0, depthBase))).setScale(2, RoundingMode.HALF_UP);

        // 5. Weighted Overall Composite Score:
        // Overall = (Market * 0.30) + (Feasibility * 0.30) + (Competition * 0.20) + (ConceptDepth * 0.20)
        BigDecimal weightedMarket = out.marketScore.multiply(new BigDecimal("0.30"));
        BigDecimal weightedFeas = out.feasibilityScore.multiply(new BigDecimal("0.30"));
        BigDecimal weightedComp = out.competitionScore.multiply(new BigDecimal("0.20"));
        BigDecimal weightedDepth = out.depthScore.multiply(new BigDecimal("0.20"));

        out.overallScore = weightedMarket.add(weightedFeas).add(weightedComp).add(weightedDepth)
                .setScale(2, RoundingMode.HALF_UP);

        // 6. Decision Tier Assignment:
        // STRONG_GO (>=80), GO (65-79), CAUTION (50-64), PIVOT (<50)
        double overall = out.overallScore.doubleValue();
        if (overall >= 80.0) {
            out.decisionTier = "STRONG_GO";
        } else if (overall >= 65.0) {
            out.decisionTier = "GO";
        } else if (overall >= 50.0) {
            out.decisionTier = "CAUTION";
        } else {
            out.decisionTier = "PIVOT";
        }

        // 7. SWOT Matrix Synthesis
        List<String> strengthsList = new ArrayList<>();
        List<String> weaknessesList = new ArrayList<>();
        List<String> oppsList = new ArrayList<>();
        List<String> threatsList = new ArrayList<>();
        List<String> risksList = new ArrayList<>();

        if (nlp.feasibilityMatchCount > 2) strengthsList.add("Strong technical foundation and architectural feasibility");
        if (nlp.marketMatchCount > 2) strengthsList.add("High resonance with target customer pain points and addressable demand");
        if (strengthsList.isEmpty()) strengthsList.add("Focused domain concept with lean initial operational footprint");

        if (!nlp.negatedPhrases.isEmpty()) {
            weaknessesList.add("Unresolved demand blockers: " + String.join(", ", nlp.negatedPhrases));
        }
        if (nlp.competitionMatchCount < 1) {
            weaknessesList.add("Unclear competitive moat or IP defensibility against incumbents");
        }
        if (weaknessesList.isEmpty()) {
            weaknessesList.add("Early stage customer validation required to prove unit economics");
        }

        oppsList.add("Rapid enterprise transition toward automated workflows and cloud telemetry");
        if (model != null && model.toLowerCase().contains("saas")) {
            oppsList.add("Predictable ARR expansion via tiered B2B subscription licenses");
        } else {
            oppsList.add("Scalable monetization expansion across adjacent vertical segments");
        }

        threatsList.add("Incumbent vendor fast-following with packaged bundle features");
        if (nlp.negatedPhrases.size() > 1) {
            threatsList.add("Market saturation risk due to customer resistance or lack of urgency");
        } else {
            threatsList.add("Macro-economic tightening lengthening enterprise budget sign-offs");
        }

        // Critical failure risks
        if (overall < 65.0) {
            risksList.add("High customer acquisition cost (CAC) exceeding lifetime value (LTV)");
            risksList.add("Solution risk: value proposition may not overcome switching friction");
        } else {
            risksList.add("Execution risk: hiring specialized engineering talent to maintain roadmap velocity");
            risksList.add("Distribution risk: building repeatable outbound B2B sales pipeline");
        }

        out.strengths = String.join(" • ", strengthsList);
        out.weaknesses = String.join(" • ", weaknessesList);
        out.opportunities = String.join(" • ", oppsList);
        out.threats = String.join(" • ", threatsList);
        out.criticalRisks = String.join("; ", risksList);

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
