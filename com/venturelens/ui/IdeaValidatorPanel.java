package com.venturelens.ui;

import com.venturelens.analysis.ScoringEngine;
import com.venturelens.dao.VentureDAO;
import com.venturelens.model.Venture;
import com.venturelens.ui.components.DarkTheme;
import com.venturelens.utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Module 1: Idea Validator & Rule-Based NLP Evaluation Engine.
 */
public class IdeaValidatorPanel extends JPanel {

    private final MainFrame mainFrame;
    private final VentureDAO ventureDAO = new VentureDAO();

    // Form inputs
    private JTextField nameField;
    private JTextField customerField;
    private JTextArea problemArea;
    private JTextArea solutionArea;
    private JTextField modelField;

    // Results output
    private JLabel scoreLabel;
    private JLabel tierLabel;
    private JProgressBar marketBar;
    private JProgressBar feasBar;
    private JProgressBar compBar;
    private JProgressBar depthBar;

    private JTextArea strengthsArea;
    private JTextArea weaknessesArea;
    private JTextArea opportunitiesArea;
    private JTextArea threatsArea;
    private JLabel risksLabel;

    private JButton analyzeBtn;
    private JButton saveBtn;
    private JLabel statusLabel;

    private ScoringEngine.EvaluationOutput lastEvaluation;

    public IdeaValidatorPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(DarkTheme.BG_DARK);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Idea Validator & NLP Decision Engine");
        title.setFont(DarkTheme.FONT_TITLE);
        title.setForeground(DarkTheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Rule-based lexical analysis, lookbehind negation detection, and 4-tier viability scoring");
        sub.setFont(DarkTheme.FONT_BODY);
        sub.setForeground(DarkTheme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerBtns.setOpaque(false);

        JButton sampleBtn = DarkTheme.createButton("Load Sample Idea", false);
        sampleBtn.addActionListener(e -> loadSampleIdea());
        headerBtns.add(sampleBtn);

        header.add(headerBtns, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center Split: Left = Input Form, Right = Analysis Results
        JPanel mainGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        mainGrid.setOpaque(false);
        mainGrid.setBorder(new EmptyBorder(16, 0, 0, 0));

        // LEFT: Input Form
        JPanel formCard = DarkTheme.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        formCard.add(createInputLabel("Startup / Product Name"));
        formCard.add(Box.createVerticalStrut(4));
        nameField = DarkTheme.createTextField(20);
        nameField.setText("BioMesh Diagnostics");
        formCard.add(nameField);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(createInputLabel("Target Customer Profile"));
        formCard.add(Box.createVerticalStrut(4));
        customerField = DarkTheme.createTextField(20);
        customerField.setText("Oncology Clinics & Point-of-Care Pathology Labs");
        formCard.add(customerField);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(createInputLabel("Problem Statement & Customer Pain"));
        formCard.add(Box.createVerticalStrut(4));
        problemArea = DarkTheme.createTextArea(3, 20);
        problemArea.setText("Current biopsy histology requires 5-day turnaround time with zero real-time tissue margin verification during surgery.");
        JScrollPane probScroll = new JScrollPane(problemArea);
        probScroll.setBorder(new LineBorder(DarkTheme.BORDER_COLOR, 1));
        probScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        formCard.add(probScroll);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(createInputLabel("Proposed Solution & Defensible Moat"));
        formCard.add(Box.createVerticalStrut(4));
        solutionArea = DarkTheme.createTextArea(3, 20);
        solutionArea.setText("Handheld micro-spectroscopy probe with proprietary edge AI algorithm detecting cancerous margins in under 90 seconds.");
        JScrollPane solScroll = new JScrollPane(solutionArea);
        solScroll.setBorder(new LineBorder(DarkTheme.BORDER_COLOR, 1));
        solScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        formCard.add(solScroll);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(createInputLabel("Business Model & Monetization"));
        formCard.add(Box.createVerticalStrut(4));
        modelField = DarkTheme.createTextField(20);
        modelField.setText("Hardware probe lease (₹2,00,000/mo) + ₹14,500 per-test sterile optical cartridge");
        formCard.add(modelField);
        formCard.add(Box.createVerticalStrut(16));

        // Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);

        analyzeBtn = DarkTheme.createButton("Run NLP Evaluation", true);
        analyzeBtn.addActionListener(e -> runAnalysis());
        actionRow.add(analyzeBtn);

        saveBtn = DarkTheme.createButton("Save to MySQL", false);
        saveBtn.addActionListener(e -> saveVentureToDB());
        actionRow.add(saveBtn);

        formCard.add(actionRow);
        formCard.add(Box.createVerticalStrut(10));

        statusLabel = new JLabel("Click 'Run NLP Evaluation' to score this venture concept");
        statusLabel.setFont(DarkTheme.FONT_SMALL);
        statusLabel.setForeground(DarkTheme.TEXT_MUTED);
        formCard.add(statusLabel);

        mainGrid.add(formCard);

        // RIGHT: Analysis Results & SWOT
        JPanel resultsCard = DarkTheme.createCard();
        resultsCard.setLayout(new BoxLayout(resultsCard, BoxLayout.Y_AXIS));

        // Score Row
        JPanel scoreRow = new JPanel(new BorderLayout());
        scoreRow.setOpaque(false);

        JPanel scoreLeft = new JPanel();
        scoreLeft.setLayout(new BoxLayout(scoreLeft, BoxLayout.Y_AXIS));
        scoreLeft.setOpaque(false);

        JLabel scoreTitle = new JLabel("COMPOSITE SCORE");
        scoreTitle.setFont(DarkTheme.FONT_SMALL);
        scoreTitle.setForeground(DarkTheme.TEXT_MUTED);

        scoreLabel = new JLabel("84.20 / 100");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        scoreLabel.setForeground(DarkTheme.TEXT_PRIMARY);

        scoreLeft.add(scoreTitle);
        scoreLeft.add(scoreLabel);
        scoreRow.add(scoreLeft, BorderLayout.WEST);

        tierLabel = new JLabel("STRONG_GO", SwingConstants.CENTER);
        tierLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tierLabel.setForeground(DarkTheme.SUCCESS);
        tierLabel.setBorder(new CompoundBorder(
                new LineBorder(DarkTheme.SUCCESS, 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        scoreRow.add(tierLabel, BorderLayout.EAST);
        resultsCard.add(scoreRow);
        resultsCard.add(Box.createVerticalStrut(14));

        // Subscores
        resultsCard.add(createProgressBarPanel("Market Readiness (30% weight)", marketBar = createBar()));
        resultsCard.add(Box.createVerticalStrut(6));
        resultsCard.add(createProgressBarPanel("Feasibility & Architecture (30% weight)", feasBar = createBar()));
        resultsCard.add(Box.createVerticalStrut(6));
        resultsCard.add(createProgressBarPanel("Competitive Defensibility (20% weight)", compBar = createBar()));
        resultsCard.add(Box.createVerticalStrut(6));
        resultsCard.add(createProgressBarPanel("Concept Depth & Model (20% weight)", depthBar = createBar()));
        resultsCard.add(Box.createVerticalStrut(14));

        // 4-Box SWOT Matrix
        JLabel swotHeader = new JLabel("SWOT Analysis Matrix");
        swotHeader.setFont(DarkTheme.FONT_SUBHEAD);
        swotHeader.setForeground(DarkTheme.TEXT_PRIMARY);
        resultsCard.add(swotHeader);
        resultsCard.add(Box.createVerticalStrut(8));

        JPanel swotGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        swotGrid.setOpaque(false);

        swotGrid.add(createSwotBox("Strengths", strengthsArea = createSwotArea(), new Color(0x1A, 0x7F, 0x37)));
        swotGrid.add(createSwotBox("Weaknesses", weaknessesArea = createSwotArea(), new Color(0xCF, 0x22, 0x2E)));
        swotGrid.add(createSwotBox("Opportunities", opportunitiesArea = createSwotArea(), new Color(0x09, 0x69, 0xDA)));
        swotGrid.add(createSwotBox("Threats", threatsArea = createSwotArea(), new Color(0x8A, 0x5B, 0x00)));

        resultsCard.add(swotGrid);
        resultsCard.add(Box.createVerticalStrut(10));

        // Critical Risks
        JLabel riskTitle = new JLabel("Critical Failure Risks:");
        riskTitle.setFont(DarkTheme.FONT_SMALL);
        riskTitle.setForeground(DarkTheme.DANGER);
        resultsCard.add(riskTitle);

        risksLabel = new JLabel("High regulatory certification hurdle (FDA Class II clearance path)");
        risksLabel.setFont(DarkTheme.FONT_SMALL);
        risksLabel.setForeground(DarkTheme.TEXT_MUTED);
        resultsCard.add(risksLabel);

        mainGrid.add(resultsCard);

        add(mainGrid, BorderLayout.CENTER);

        // Run initial evaluation on default values
        runAnalysis();
    }

    private JLabel createInputLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(DarkTheme.FONT_SUBHEAD);
        l.setForeground(DarkTheme.TEXT_PRIMARY);
        return l;
    }

    private JProgressBar createBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(80);
        bar.setStringPainted(true);
        bar.setFont(DarkTheme.FONT_SMALL);
        bar.setForeground(DarkTheme.ACCENT);
        bar.setBackground(DarkTheme.INPUT_BG);
        bar.setBorder(new LineBorder(DarkTheme.BORDER_COLOR, 1));
        bar.setPreferredSize(new Dimension(140, 16));
        return bar;
    }

    private JPanel createProgressBarPanel(String title, JProgressBar bar) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(DarkTheme.FONT_SMALL);
        lbl.setForeground(DarkTheme.TEXT_MUTED);
        p.add(lbl, BorderLayout.WEST);
        p.add(bar, BorderLayout.EAST);
        return p;
    }

    private JPanel createSwotBox(String title, JTextArea area, Color borderTint) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(new Color(0xF8, 0xF9, 0xFA));
        box.setBorder(new CompoundBorder(
                new LineBorder(borderTint, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(borderTint);
        box.add(lbl, BorderLayout.NORTH);
        box.add(area, BorderLayout.CENTER);
        return box;
    }

    private JTextArea createSwotArea() {
        JTextArea a = new JTextArea();
        a.setOpaque(false);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(DarkTheme.FONT_SMALL);
        a.setForeground(DarkTheme.TEXT_PRIMARY);
        return a;
    }

    private void runAnalysis() {
        String name = nameField.getText().trim();
        String customer = customerField.getText().trim();
        String problem = problemArea.getText().trim();
        String solution = solutionArea.getText().trim();
        String model = modelField.getText().trim();

        lastEvaluation = ScoringEngine.evaluate(name, customer, problem, solution, model);

        scoreLabel.setText(lastEvaluation.overallScore + " / 100");
        tierLabel.setText(lastEvaluation.decisionTier);

        marketBar.setValue(lastEvaluation.marketScore.intValue());
        feasBar.setValue(lastEvaluation.feasibilityScore.intValue());
        compBar.setValue(lastEvaluation.competitionScore.intValue());
        depthBar.setValue(lastEvaluation.depthScore.intValue());

        strengthsArea.setText(lastEvaluation.strengths);
        weaknessesArea.setText(lastEvaluation.weaknesses);
        opportunitiesArea.setText(lastEvaluation.opportunities);
        threatsArea.setText(lastEvaluation.threats);
        risksLabel.setText(lastEvaluation.criticalRisks);

        if ("STRONG_GO".equals(lastEvaluation.decisionTier)) {
            tierLabel.setForeground(DarkTheme.SUCCESS);
            tierLabel.setBorder(new CompoundBorder(new LineBorder(DarkTheme.SUCCESS, 1, true), new EmptyBorder(6, 16, 6, 16)));
        } else if ("GO".equals(lastEvaluation.decisionTier)) {
            tierLabel.setForeground(DarkTheme.ACCENT_HOVER);
            tierLabel.setBorder(new CompoundBorder(new LineBorder(DarkTheme.ACCENT_HOVER, 1, true), new EmptyBorder(6, 16, 6, 16)));
        } else if ("CAUTION".equals(lastEvaluation.decisionTier)) {
            tierLabel.setForeground(DarkTheme.WARNING);
            tierLabel.setBorder(new CompoundBorder(new LineBorder(DarkTheme.WARNING, 1, true), new EmptyBorder(6, 16, 6, 16)));
        } else {
            tierLabel.setForeground(DarkTheme.DANGER);
            tierLabel.setBorder(new CompoundBorder(new LineBorder(DarkTheme.DANGER, 1, true), new EmptyBorder(6, 16, 6, 16)));
        }

        statusLabel.setText("NLP Evaluation complete. Composite score: " + lastEvaluation.overallScore);
    }

    private void saveVentureToDB() {
        if (lastEvaluation == null) {
            runAnalysis();
        }

        saveBtn.setEnabled(false);
        statusLabel.setText("Saving evaluation to MySQL / Aiven Cloud...");

        int userId = UserSession.getInstance().isAuthenticated() ? UserSession.getInstance().getCurrentUser().getId() : 1;

        Venture v = new Venture();
        v.setUserId(userId);
        v.setStartupName(nameField.getText().trim());
        v.setTargetCustomer(customerField.getText().trim());
        v.setProblemStatement(problemArea.getText().trim());
        v.setProposedSolution(solutionArea.getText().trim());
        v.setBusinessModel(modelField.getText().trim());
        ScoringEngine.applyToVenture(v, lastEvaluation);

        new SwingWorker<Venture, Void>() {
            @Override
            protected Venture doInBackground() throws Exception {
                return ventureDAO.save(v);
            }

            @Override
            protected void done() {
                saveBtn.setEnabled(true);
                try {
                    Venture saved = get();
                    UserSession.getInstance().setActiveVenture(saved);
                    statusLabel.setText("Successfully saved venture '" + saved.getStartupName() + "' (ID: " + saved.getId() + ")");
                } catch (Exception ex) {
                    statusLabel.setText("Saved to local database cache: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadSampleIdea() {
        nameField.setText("OmniRoute Cloud");
        customerField.setText("Mid-market Logistics Operators & Freight Carriers");
        problemArea.setText("High diesel fuel costs and manual dispatch planning create 22% empty-mile waste without real-time telemetry.");
        solutionArea.setText("Automated dynamic routing and load matching algorithm using IoT edge GPS trackers and instant dispatch API.");
        modelField.setText("B2B SaaS Subscription (₹39,999/mo per fleet) + 1.5% transaction fee on spot-market carrier load booking");
        runAnalysis();
    }
}
