package com.venturelens.ui;

import com.venturelens.model.Venture;
import com.venturelens.ui.components.DarkTheme;
import com.venturelens.utils.ReportExporter;
import com.venturelens.utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Module 4: PitchCraft 10-Slide Investor Deck & Blueprint Exporter.
 */
public class PitchCraftPanel extends JPanel implements UserSession.SessionListener {

    private final MainFrame mainFrame;
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private JTextArea previewArea;
    private JLabel ventureTitleLabel;
    private JLabel statusLabel;
    private JButton exportMdBtn;
    private JButton exportHtmlBtn;

    public PitchCraftPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(DarkTheme.BG_DARK);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        UserSession.getInstance().addListener(this);
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("PitchCraft — Investor Blueprint Exporter");
        title.setFont(DarkTheme.FONT_TITLE);
        title.setForeground(DarkTheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Consolidated 10-slide startup deck generator with Markdown & print-styled HTML output");
        sub.setFont(DarkTheme.FONT_BODY);
        sub.setForeground(DarkTheme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        // Action Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        exportMdBtn = DarkTheme.createButton("Export Markdown (.md)", true);
        exportMdBtn.addActionListener(e -> doExportMarkdown());
        btnRow.add(exportMdBtn);

        exportHtmlBtn = DarkTheme.createButton("Export Printable HTML", false);
        exportHtmlBtn.addActionListener(e -> doExportHtml());
        btnRow.add(exportHtmlBtn);

        header.add(btnRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center: Preview Card
        JPanel card = DarkTheme.createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new CompoundBorder(
                new LineBorder(DarkTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        ventureTitleLabel = new JLabel("10-Slide Investor Blueprint Preview");
        ventureTitleLabel.setFont(DarkTheme.FONT_HEADER);
        ventureTitleLabel.setForeground(DarkTheme.TEXT_PRIMARY);
        cardHeader.add(ventureTitleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Ready to export");
        statusLabel.setFont(DarkTheme.FONT_SMALL);
        statusLabel.setForeground(DarkTheme.TEXT_MUTED);
        cardHeader.add(statusLabel, BorderLayout.EAST);

        card.add(cardHeader, BorderLayout.NORTH);

        previewArea = new JTextArea();
        previewArea.setBackground(DarkTheme.INPUT_BG);
        previewArea.setForeground(DarkTheme.TEXT_PRIMARY);
        previewArea.setCaretColor(DarkTheme.TEXT_PRIMARY);
        previewArea.setFont(DarkTheme.FONT_MONO);
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(previewArea);
        scroll.setBorder(new LineBorder(DarkTheme.BORDER_COLOR, 1));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        refreshPreview();
    }

    private void refreshPreview() {
        Venture v = UserSession.getInstance().getActiveVenture();
        if (v == null) {
            previewArea.setText("// No active venture selected. Go to Overview or Idea Validator to create or select a venture.");
            return;
        }

        ventureTitleLabel.setText("10-Slide Investor Blueprint: " + v.getStartupName());

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================================\n");
        sb.append(" PITCHCRAFT INVESTOR BLUEPRINT: ").append(v.getStartupName().toUpperCase()).append("\n");
        sb.append("=========================================================================\n\n");

        sb.append("[ SLIDE 1: Executive Summary & Vision ]\n");
        sb.append("  * Startup Name: ").append(v.getStartupName()).append("\n");
        sb.append("  * Target Customer: ").append(v.getTargetCustomer()).append("\n");
        sb.append("  * Recommendation: ").append(v.getDecisionTier()).append(" (Score: ").append(v.getOverallScore()).append("/100)\n\n");

        sb.append("[ SLIDE 2: Core Customer Problem ]\n");
        sb.append("  ").append(v.getProblemStatement()).append("\n\n");

        sb.append("[ SLIDE 3: Proposed Solution & Defensibility ]\n");
        sb.append("  ").append(v.getProposedSolution()).append("\n\n");

        sb.append("[ SLIDE 4: Market Validation & Subscores ]\n");
        sb.append("  * Market Readiness: ").append(v.getMarketScore()).append("/100\n");
        sb.append("  * Technical Feasibility: ").append(v.getFeasibilityScore()).append("/100\n");
        sb.append("  * Competitive Moat: ").append(v.getCompetitionScore()).append("/100\n");
        sb.append("  * Concept Depth: ").append(v.getDepthScore()).append("/100\n\n");

        sb.append("[ SLIDE 5: Business Model & Monetization ]\n");
        sb.append("  ").append(v.getBusinessModel()).append("\n\n");

        sb.append("[ SLIDE 6: SWOT Analysis Matrix ]\n");
        sb.append("  * Strengths:     ").append(v.getStrengths()).append("\n");
        sb.append("  * Weaknesses:    ").append(v.getWeaknesses()).append("\n");
        sb.append("  * Opportunities: ").append(v.getOpportunities()).append("\n");
        sb.append("  * Threats:       ").append(v.getThreats()).append("\n\n");

        sb.append("[ SLIDE 7: Critical Failure Risks ]\n");
        sb.append("  ").append(v.getCriticalRisks()).append("\n\n");

        sb.append("[ SLIDE 8: CapTable & Ownership Distribution ]\n");
        sb.append("  * Founders Retain: ").append(v.getFounderEquity()).append("%\n");
        sb.append("  * ESOP Talent Pool: ").append(v.getEsopPool()).append("%\n");
        sb.append("  * Round Investors:  ").append(v.getInvestorEquity()).append("%\n\n");

        sb.append("[ SLIDE 9: Valuation & Investment Round ]\n");
        sb.append("  * Pre-Money Valuation: ").append(CURRENCY.format(v.getPreMoneyValuation())).append("\n");
        sb.append("  * Investment Round Ask: ").append(CURRENCY.format(v.getInvestmentAmount())).append("\n");
        sb.append("  * Post-Money Valuation: ").append(CURRENCY.format(v.getPostMoneyValuation())).append("\n\n");

        sb.append("[ SLIDE 10: BurnWatch Financial Health & Runway ]\n");
        sb.append("  * Cash Reserves: ").append(CURRENCY.format(v.getCurrentCashBalance())).append("\n");
        sb.append("  * Net Monthly Burn: ").append(CURRENCY.format(v.getMonthlyBurn())).append("/mo\n");
        sb.append("  * Survival Runway: ").append(v.getRunwayMonths()).append(" Months\n");

        previewArea.setText(sb.toString());
        previewArea.setCaretPosition(0);
    }

    private void doExportMarkdown() {
        Venture v = UserSession.getInstance().getActiveVenture();
        if (v == null) {
            statusLabel.setText("No active venture to export.");
            return;
        }

        try {
            String safeName = v.getStartupName().replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = safeName + "_pitch_blueprint.md";
            Path path = ReportExporter.exportToMarkdown(v, fileName);
            statusLabel.setText("Exported Markdown: " + path.toAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Successfully exported 10-slide Markdown deck to:\n" + path.toAbsolutePath(),
                    "PitchCraft Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            statusLabel.setText("Export error: " + ex.getMessage());
        }
    }

    private void doExportHtml() {
        Venture v = UserSession.getInstance().getActiveVenture();
        if (v == null) {
            statusLabel.setText("No active venture to export.");
            return;
        }

        try {
            String safeName = v.getStartupName().replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = safeName + "_pitch_blueprint.html";
            Path path = ReportExporter.exportToPrintableHtml(v, fileName);
            statusLabel.setText("Exported HTML: " + path.toAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Successfully exported Printable HTML to:\n" + path.toAbsolutePath() +
                    "\n\nTip: Open in your browser and press Ctrl+P / Cmd+P to save as 1-click PDF!",
                    "PitchCraft Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            statusLabel.setText("Export error: " + ex.getMessage());
        }
    }

    @Override
    public void onUserLoggedIn(com.venturelens.model.User user) {
        refreshPreview();
    }

    @Override
    public void onUserLoggedOut() {
        previewArea.setText("");
    }

    @Override
    public void onActiveVentureChanged(Venture venture) {
        refreshPreview();
    }
}
