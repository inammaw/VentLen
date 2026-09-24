package com.venturelens.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure Java Rule-Based Natural Language Processing (NLP) Engine.
 * Implements tokenization, punctuation removal, English stop-word filtering,
 * regex lookbehind negation detection, and categorized domain vocabulary matching.
 * Strictly standard JDK without OpenNLP, Stanford CoreNLP, or third-party libraries.
 */
public class RuleBasedNlpEngine {

    // Standard English stop words
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can",
            "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
            "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
            "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him",
            "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't",
            "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "of", "off",
            "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same",
            "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that",
            "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they",
            "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until",
            "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what",
            "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's",
            "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours",
            "yourself", "yourselves"
    ));

    // Categorized Domain Dictionaries
    private static final Set<String> MARKET_TERMS = new HashSet<>(Arrays.asList(
            "market", "customer", "demand", "tam", "sam", "som", "growth", "b2b", "b2c", "saas",
            "enterprise", "retention", "churn", "acquisition", "ltv", "cac", "recurring", "monetization",
            "expansion", "pain", "need", "users", "audience", "scale", "subscribers", "contracts"
    ));

    private static final Set<String> FEASIBILITY_TERMS = new HashSet<>(Arrays.asList(
            "api", "algorithm", "cloud", "architecture", "database", "scalable", "mvp", "prototype",
            "engineering", "automation", "integration", "workflow", "backend", "frontend", "infrastructure",
            "patent", "proprietary", "pipeline", "platform", "tested", "operational", "developer", "hardware"
    ));

    private static final Set<String> COMPETITION_TERMS = new HashSet<>(Arrays.asList(
            "moat", "advantage", "differentiation", "competitor", "incumbent", "barrier", "defensible",
            "exclusive", "first-mover", "network-effect", "superior", "cheaper", "faster", "patent",
            "unmatched", "efficiency", "niche", "monopoly", "leadership"
    ));

    // Regex for punctuation removal and clean tokenization
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*");

    // Lookbehind / Prefix Negation Pattern
    // Detects: "no demand", "lack of competition", "not feasible", "without market", "hardly viable", "zero differentiation"
    private static final Pattern NEGATION_PATTERN = Pattern.compile(
            "(?i)\\b(no|not|never|neither|nor|barely|hardly|scarcely|without|lack\\s+of|zero)\\s+([a-z]+(-[a-z]+)?)"
    );

    public static class NlpResult {
        public List<String> rawTokens = new ArrayList<>();
        public List<String> filteredTokens = new ArrayList<>();
        public List<String> negatedPhrases = new ArrayList<>();
        public int marketMatchCount = 0;
        public int feasibilityMatchCount = 0;
        public int competitionMatchCount = 0;
        public int positiveMatches = 0;
        public int negativeMatches = 0;
    }

    /**
     * Executes the NLP pipeline on the input text.
     */
    public static NlpResult analyze(String text) {
        NlpResult result = new NlpResult();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }

        String lowerText = text.toLowerCase();

        // 1. Negation detection via regex lookahead/lookbehind
        Matcher negMatcher = NEGATION_PATTERN.matcher(lowerText);
        Set<String> negatedWords = new HashSet<>();
        while (negMatcher.find()) {
            String fullNegation = negMatcher.group(0);
            String targetWord = negMatcher.group(2);
            result.negatedPhrases.add(fullNegation);
            negatedWords.add(targetWord);
            result.negativeMatches++;
        }

        // 2. Tokenization and Punctuation removal
        Matcher wordMatcher = WORD_PATTERN.matcher(lowerText);
        while (wordMatcher.find()) {
            String token = wordMatcher.group();
            result.rawTokens.add(token);

            // Filter stop words
            if (!STOP_WORDS.contains(token) && token.length() > 2) {
                result.filteredTokens.add(token);

                boolean isNegated = negatedWords.contains(token);

                // Check domain dictionaries
                if (MARKET_TERMS.contains(token)) {
                    if (isNegated) result.negativeMatches++;
                    else { result.marketMatchCount++; result.positiveMatches++; }
                }
                if (FEASIBILITY_TERMS.contains(token)) {
                    if (isNegated) result.negativeMatches++;
                    else { result.feasibilityMatchCount++; result.positiveMatches++; }
                }
                if (COMPETITION_TERMS.contains(token)) {
                    if (isNegated) result.negativeMatches++;
                    else { result.competitionMatchCount++; result.positiveMatches++; }
                }
            }
        }

        return result;
    }

    public static Set<String> getMarketTerms() { return Collections.unmodifiableSet(MARKET_TERMS); }
    public static Set<String> getFeasibilityTerms() { return Collections.unmodifiableSet(FEASIBILITY_TERMS); }
    public static Set<String> getCompetitionTerms() { return Collections.unmodifiableSet(COMPETITION_TERMS); }
}
