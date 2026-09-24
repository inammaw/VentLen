package com.venturelens.analysis;

import java.util.*;
import java.util.regex.Pattern;

public final class StartupEvaluator {

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[a-zA-Z]+(?:'[a-zA-Z]+)?");

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "if", "then",
            "is", "are", "was", "were", "be", "been", "being",
            "to", "of", "in", "on", "for", "with", "by", "at",
            "from", "as", "into", "through", "during", "before",
            "after", "above", "below", "this", "that", "these",
            "those", "it", "its", "they", "them", "their", "we",
            "our", "you", "your", "will", "would", "can", "could",
            "should", "may", "might", "very", "also", "than",
            "have", "has", "had", "do", "does", "did"
    );

    private static final Set<String> MARKET_TERMS = Set.of(
            "market", "customer", "customers", "users", "demand",
            "need", "needs", "problem", "pain", "segment",
            "industry", "business", "consumer", "consumers",
            "growth", "marketplace", "sales", "revenue",
            "subscription", "buyer", "buyers", "audience",
            "target", "population", "adoption", "trend"
    );

    private static final Set<String> COMPETITION_TERMS = Set.of(
            "competitor", "competitors", "competition", "competitive",
            "alternative", "alternatives", "rival", "rivals",
            "existing", "marketplace", "monopoly", "advantage",
            "differentiation", "different", "unique", "pricing",
            "barrier", "barriers", "moat", "incumbent"
    );

    private static final Set<String> FEASIBILITY_TERMS = Set.of(
            "build", "built", "develop", "development", "technology",
            "technical", "prototype", "software",
            "hardware", "api", "integration", "deployment",
            "implementation", "team", "developer", "developers",
            "cost", "budget", "infrastructure", "scalable",
            "scale", "testing", "test", "launch", "production"
    );

    private static final Set<String> POSITIVE_TERMS = Set.of(
            "strong", "clear", "large", "growing", "validated",
            "proven", "unique", "scalable", "profitable",
            "valuable", "efficient", "simple", "affordable",
            "demand", "need", "advantage", "traction",
            "revenue", "growth", "solution", "solves"
    );

    private static final Set<String> NEGATIVE_TERMS = Set.of(
            "weak", "small", "unclear", "declining", "expensive",
            "difficult", "complex", "risky", "risk", "uncertain",
            "unproven", "limited", "low", "high", "problem",
            "failure", "fail", "costly", "unstable", "crowded"
    );

    private static final Set<String> NEGATION_TERMS = Set.of(
            "no", "not", "never", "none", "neither", "without",
            "lack", "lacks", "lacking", "cannot", "can't",
            "isn't", "aren't", "doesn't", "don't", "won't"
    );

    private static final Set<String> CRITICAL_RISK_TERMS = Set.of(
            "illegal", "fraud", "unsafe", "impossible",
            "no demand", "no customers", "no market",
            "not feasible", "cannot build", "cannot scale",
            "regulatory ban", "bankrupt"
    );

    private static final int NEGATION_WINDOW = 3;

    private StartupEvaluator() {
    }

    public static EvaluationResult evaluate(
            String startupName,
            String targetCustomer,
            String problemStatement,
            String proposedSolution,
            String businessModel) {

        String combinedText = String.join(" ",
                safe(startupName),
                safe(targetCustomer),
                safe(problemStatement),
                safe(proposedSolution),
                safe(businessModel)
        );

        List<String> tokens = tokenize(combinedText);

        EvidenceCounter counter = new EvidenceCounter();

        scan(tokens, counter);

        double marketScore =
                calculateCategoryScore(
                        counter.marketPositive,
                        counter.marketNegative,
                        counter.marketCoverage
                );

        double competitionScore =
                calculateCategoryScore(
                        counter.competitionPositive,
                        counter.competitionNegative,
                        counter.competitionCoverage
                );

        double feasibilityScore =
                calculateCategoryScore(
                        counter.feasibilityPositive,
                        counter.feasibilityNegative,
                        counter.feasibilityCoverage
                );

        double conceptDepthScore =
                calculateConceptDepth(tokens, counter);

        double overall =
                marketScore * 0.30
              + feasibilityScore * 0.30
              + competitionScore * 0.20
              + conceptDepthScore * 0.20;

        overall = clamp(overall, 0, 100);

        DecisionTier tier = determineTier(overall);

        List<String> strengths = buildStrengths(counter);
        List<String> weaknesses = buildWeaknesses(counter);
        List<String> opportunities = buildOpportunities(counter);
        List<String> threats = buildThreats(counter);
        List<String> risks = buildCriticalRisks(tokens);

        return new EvaluationResult(
                startupName,
                marketScore,
                feasibilityScore,
                competitionScore,
                conceptDepthScore,
                overall,
                tier,
                strengths,
                weaknesses,
                opportunities,
                threats,
                risks
        );
    }

    private static void scan(
            List<String> tokens,
            EvidenceCounter counter) {

        for (int i = 0; i < tokens.size(); i++) {

            String token = tokens.get(i);

            boolean negated = isNegated(tokens, i);

            if (MARKET_TERMS.contains(token)) {
                counter.marketCoverage++;

                if (negated) {
                    counter.marketNegative++;
                } else {
                    counter.marketPositive++;
                }
            }

            if (COMPETITION_TERMS.contains(token)) {
                counter.competitionCoverage++;

                if (negated) {
                    counter.competitionNegative++;
                } else {
                    counter.competitionPositive++;
                }
            }

            if (FEASIBILITY_TERMS.contains(token)) {
                counter.feasibilityCoverage++;

                if (negated) {
                    counter.feasibilityNegative++;
                } else {
                    counter.feasibilityPositive++;
                }
            }

            if (POSITIVE_TERMS.contains(token)) {

                if (negated) {
                    counter.negativeSignals++;
                } else {
                    counter.positiveSignals++;
                }
            }

            if (NEGATIVE_TERMS.contains(token)) {

                if (negated) {
                    counter.positiveSignals++;
                } else {
                    counter.negativeSignals++;
                }
            }
        }
    }

    private static boolean isNegated(
            List<String> tokens,
            int index) {

        int start = Math.max(0, index - NEGATION_WINDOW);

        for (int i = start; i < index; i++) {
            if (NEGATION_TERMS.contains(tokens.get(i))) {
                return true;
            }
        }

        return false;
    }

    private static double calculateCategoryScore(
            int positive,
            int negative,
            int coverage) {

        if (coverage == 0) {
            return 40.0;
        }

        double evidence = positive * 10.0 - negative * 12.0;

        double coverageBonus =
                Math.min(20.0, coverage * 3.0);

        double score =
                50.0 + evidence + coverageBonus;

        return clamp(score, 0, 100);
    }

    private static double calculateConceptDepth(
            List<String> tokens,
            EvidenceCounter counter) {

        if (tokens.isEmpty()) {
            return 0;
        }

        Set<String> uniqueTokens =
                new HashSet<>(tokens);

        int meaningfulTerms = 0;

        for (String token : uniqueTokens) {
            if (!STOP_WORDS.contains(token)) {
                meaningfulTerms++;
            }
        }

        double vocabularyScore =
                Math.min(35.0, meaningfulTerms * 1.5);

        int categoryCoverage = 0;

        if (counter.marketCoverage > 0) {
            categoryCoverage++;
        }

        if (counter.competitionCoverage > 0) {
            categoryCoverage++;
        }

        if (counter.feasibilityCoverage > 0) {
            categoryCoverage++;
        }

        double categoryScore =
                categoryCoverage * 15.0;

        double positiveQuality =
                Math.min(
                        20.0,
                        counter.positiveSignals * 2.0
                );

        return clamp(
                vocabularyScore
                + categoryScore
                + positiveQuality,
                0,
                100
        );
    }

    private static DecisionTier determineTier(
            double score) {

        if (score >= 80) {
            return DecisionTier.STRONG_GO;
        }

        if (score >= 65) {
            return DecisionTier.GO;
        }

        if (score >= 50) {
            return DecisionTier.CAUTION;
        }

        return DecisionTier.PIVOT;
    }

    private static List<String> buildStrengths(
            EvidenceCounter counter) {

        List<String> result = new ArrayList<>();

        if (counter.marketPositive >= 2) {
            result.add("Clear market-demand indicators detected.");
        }

        if (counter.competitionPositive >= 2) {
            result.add("Competition and differentiation are discussed.");
        }

        if (counter.feasibilityPositive >= 2) {
            result.add("Technical implementation indicators are present.");
        }

        if (counter.positiveSignals >= 3) {
            result.add("Multiple positive business signals detected.");
        }

        return result;
    }

    private static List<String> buildWeaknesses(
            EvidenceCounter counter) {

        List<String> result = new ArrayList<>();

        if (counter.marketCoverage == 0) {
            result.add("Market evidence is insufficient.");
        }

        if (counter.competitionCoverage == 0) {
            result.add("Competitive landscape is not clearly described.");
        }

        if (counter.feasibilityCoverage == 0) {
            result.add("Technical feasibility is insufficiently described.");
        }

        if (counter.negativeSignals > counter.positiveSignals) {
            result.add("Negative signals outweigh positive signals.");
        }

        return result;
    }

    private static List<String> buildOpportunities(
            EvidenceCounter counter) {

        List<String> result = new ArrayList<>();

        if (counter.marketCoverage > 0) {
            result.add("Further customer validation could strengthen market evidence.");
        }

        if (counter.competitionCoverage == 0) {
            result.add("Competitor research could reveal differentiation opportunities.");
        }

        if (counter.feasibilityCoverage > 0) {
            result.add("Prototype development can validate technical assumptions.");
        }

        return result;
    }

    private static List<String> buildThreats(
            EvidenceCounter counter) {

        List<String> result = new ArrayList<>();

        if (counter.competitionNegative > 0) {
            result.add("Potential competitive pressure detected.");
        }

        if (counter.feasibilityNegative > 0) {
            result.add("Potential implementation challenges detected.");
        }

        if (counter.marketNegative > 0) {
            result.add("Potential market-demand concerns detected.");
        }

        return result;
    }

    private static List<String> buildCriticalRisks(
            List<String> tokens) {

        List<String> risks = new ArrayList<>();

        for (String risk : CRITICAL_RISK_TERMS) {

            String[] words = risk.split(" ");

            if (containsPhrase(tokens, words)) {
                risks.add(capitalize(risk));
            }
        }

        return risks;
    }

    private static boolean containsPhrase(
            List<String> tokens,
            String[] phrase) {

        if (phrase.length > tokens.size()) {
            return false;
        }

        for (int i = 0;
             i <= tokens.size() - phrase.length;
             i++) {

            boolean match = true;

            for (int j = 0; j < phrase.length; j++) {
                if (!tokens.get(i + j).equals(phrase[j])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return true;
            }
        }

        return false;
    }

    private static List<String> tokenize(
            String text) {

        List<String> tokens = new ArrayList<>();

        var matcher = TOKEN_PATTERN.matcher(
                text.toLowerCase(Locale.ROOT)
        );

        while (matcher.find()) {

            String token = matcher.group();

            if (!STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static double clamp(
            double value,
            double min,
            double max) {

        return Math.max(min, Math.min(max, value));
    }

    private static String capitalize(
            String text) {

        if (text == null || text.isEmpty()) {
            return text;
        }

        return Character.toUpperCase(text.charAt(0))
                + text.substring(1);
    }

    private static final class EvidenceCounter {

        int marketPositive;
        int marketNegative;
        int marketCoverage;

        int competitionPositive;
        int competitionNegative;
        int competitionCoverage;

        int feasibilityPositive;
        int feasibilityNegative;
        int feasibilityCoverage;

        int positiveSignals;
        int negativeSignals;
    }
}
