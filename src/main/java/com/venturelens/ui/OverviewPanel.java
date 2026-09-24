package com.venturelens.ui;

import com.venturelens.dao.VentureDAO;
import com.venturelens.model.Venture;
import com.venturelens.ui.components.DarkTheme;
import com.venturelens.utils.DatabaseConnection;
import com.venturelens.utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Overview & Health Snapshot Dashboard.
 */
public class OverviewPanel extends JPanel implements UserSession.SessionListener {

    private final MainFrame mainFrame;
    private final VentureDAO ventureDAO = new VentureDAO();
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private JComboBox<Venture> ventureComboBox;
    private JLabel scoreMetricLabel;
    private JLabel decisionBadgeLabel;
    private JLabel valuationMetricLabel;
    private JLabel founderEqMetricLabel;
    private JLabel runwayMetricLabel;
    private JLabel burnMetricLabel;
    private JLabel dbStatusLabel;

    public OverviewPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(DarkTheme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        UserSession.getInstance().addListener(this);
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Venture Health & Decision Intelligence");
        title.setFont(DarkTheme.FONT_TITLE);
        title.setForeground(DarkTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Comprehensive snapshot of validation scores, equity dilution, and runway");
        subtitle.setFont(DarkTheme.FONT_BODY);
        subtitle.setForeground(DarkTheme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Right side: Active Venture Selector & Refresh
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JLabel selectLbl = new JLabel("Active Venture:");
        selectLbl.setFont(DarkTheme.FONT_SUBHEAD);
        selectLbl.setForeground(DarkTheme.TEXT_PRIMARY);
        controls.add(selectLbl);

        ventureComboBox = new JComboBox<>();
        ventureComboBox.setBackground(DarkTheme.CARD_BG);
        ventureComboBox.setForeground(DarkTheme.TEXT_PRIMARY);
        ventureComboBox.setFont(DarkTheme.FONT_BODY);
        ventureComboBox.setPreferredSize(new Dimension(240, 36));
        ventureComboBox.addActionListener(e -> {
            Venture selected = (Venture) ventureComboBox.getSelectedItem();
            if (selected != null) {
                UserSession.getInstance().setActiveVenture(selected);
                updateDashboard(selected);
            }
        });
        controls.add(ventureComboBox);

        JButton refreshBtn = DarkTheme.createButton("Reload DB", false);
        refreshBtn.addActionListener(e -> reloadVenturesFromDB());
        controls.add(refreshBtn);

        headerPanel.add(controls, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Metric Bento Grid + DB Status
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Row of 3 Metric Cards
        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsRow.setOpaque(false);
        metricsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Card 1: Decision Intelligence
        JPanel card1 = DarkTheme.createCard();
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        card1.add(createCardLabel("DECISION SCORE & TIER"));
        card1.add(Box.createVerticalStrut(8));
        scoreMetricLabel = new JLabel("81.10 / 100");
        scoreMetricLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        scoreMetricLabel.setForeground(DarkTheme.TEXT_PRIMARY);
        card1.add(scoreMetricLabel);
        card1.add(Box.createVerticalStrut(4));
        decisionBadgeLabel = new JLabel("STRONG_GO");
        decisionBadgeLabel.setFont(DarkTheme.FONT_BOLD);
        decisionBadgeLabel.setForeground(DarkTheme.SUCCESS);
        card1.add(decisionBadgeLabel);
        metricsRow.add(card1);

        // Card 2: CapTable & Valuation
        JPanel card2 = DarkTheme.createCard();
        card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));
        card2.add(createCardLabel("POST-MONEY VALUATION"));
        card2.add(Box.createVerticalStrut(8));
        valuationMetricLabel = new JLabel("₹95,00,000");
        valuationMetricLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valuationMetricLabel.setForeground(DarkTheme.TEXT_PRIMARY);
        card2.add(valuationMetricLabel);
        card2.add(Box.createVerticalStrut(4));
        founderEqMetricLabel = new JLabel("Founders Retain: 75.0%");
        founderEqMetricLabel.setFont(DarkTheme.FONT_SUBHEAD);
        founderEqMetricLabel.setForeground(DarkTheme.ACCENT_HOVER);
        card2.add(founderEqMetricLabel);
        metricsRow.add(card2);

        // Card 3: BurnWatch Runway
        JPanel card3 = DarkTheme.createCard();
        card3.setLayout(new BoxLayout(card3, BoxLayout.Y_AXIS));
        card3.add(createCardLabel("CASH RUNWAY"));
        card3.add(Box.createVerticalStrut(8));
        runwayMetricLabel = new JLabel("17.6 Months");
        runwayMetricLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        runwayMetricLabel.setForeground(DarkTheme.SUCCESS);
        card3.add(runwayMetricLabel);
        card3.add(Box.createVerticalStrut(4));
        burnMetricLabel = new JLabel("Net Burn: ₹68,000 / mo");
        burnMetricLabel.setFont(DarkTheme.FONT_SUBHEAD);
        burnMetricLabel.setForeground(DarkTheme.TEXT_MUTED);
        card3.add(burnMetricLabel);
        metricsRow.add(card3);

        center.add(metricsRow);
        center.add(Box.createVerticalStrut(20));

        // Quick Navigation Tiles
        JPanel navTiles = DarkTheme.createCard();
        navTiles.setLayout(new BorderLayout());

        JLabel actionHeader = new JLabel("Direct Intelligence Modules");
        actionHeader.setFont(DarkTheme.FONT_HEADER);
        actionHeader.setForeground(DarkTheme.TEXT_PRIMARY);
        navTiles.add(actionHeader, BorderLayout.NORTH);

        JPanel tileButtons = new JPanel(new GridLayout(1, 4, 12, 0));
        tileButtons.setOpaque(false);
        tileButtons.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btn1 = DarkTheme.createButton("Idea Validator", true);
        btn1.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_VALIDATOR));
        tileButtons.add(btn1);

        JButton btn2 = DarkTheme.createButton("CapTable Sim", false);
        btn2.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_CAPTABLE));
        tileButtons.add(btn2);

        JButton btn3 = DarkTheme.createButton("BurnWatch Ledger", false);
        btn3.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_BURNWATCH));
        tileButtons.add(btn3);

        JButton btn4 = DarkTheme.createButton("PitchCraft Deck", false);
        btn4.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_PITCHCRAFT));
        tileButtons.add(btn4);

        navTiles.add(tileButtons, BorderLayout.CENTER);
        center.add(navTiles);
        center.add(Box.createVerticalStrut(20));

        // Database Connectivity Status Bar
        JPanel dbBar = DarkTheme.createCard();
        dbBar.setLayout(new BorderLayout());
        dbBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel dbTitle = new JLabel("Aiven Cloud MySQL Connection:");
        dbTitle.setFont(DarkTheme.FONT_SUBHEAD);
        dbTitle.setForeground(DarkTheme.TEXT_PRIMARY);
        dbBar.add(dbTitle, BorderLayout.WEST);

        dbStatusLabel = new JLabel("Testing connection...");
        dbStatusLabel.setFont(DarkTheme.FONT_SUBHEAD);
        dbStatusLabel.setForeground(DarkTheme.WARNING);
        dbBar.add(dbStatusLabel, BorderLayout.EAST);

        center.add(dbBar);

        add(center, BorderLayout.CENTER);

        testAivenConnectionAsync();
    }

    private JLabel createCardLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(DarkTheme.FONT_SMALL);
        l.setForeground(DarkTheme.TEXT_MUTED);
        return l;
    }

    public void reloadVenturesFromDB() {
        int userId = UserSession.getInstance().isAuthenticated() ? UserSession.getInstance().getCurrentUser().getId() : 1;

        new SwingWorker<List<Venture>, Void>() {
            @Override
            protected List<Venture> doInBackground() throws Exception {
                return ventureDAO.findAllByUserId(userId);
            }

            @Override
            protected void done() {
                try {
                    List<Venture> list = get();
                    ventureComboBox.removeAllItems();
                    for (Venture v : list) {
                        ventureComboBox.addItem(v);
                    }
                    if (!list.isEmpty()) {
                        ventureComboBox.setSelectedIndex(0);
                        updateDashboard(list.get(0));
                        UserSession.getInstance().setActiveVenture(list.get(0));
                    }
                } catch (Exception ex) {
                    System.err.println("Could not reload ventures: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateDashboard(Venture v) {
        if (v == null) return;
        scoreMetricLabel.setText(v.getOverallScore() + " / 100");
        decisionBadgeLabel.setText(v.getDecisionTier());
        if ("STRONG_GO".equals(v.getDecisionTier())) {
            decisionBadgeLabel.setForeground(DarkTheme.SUCCESS);
        } else if ("GO".equals(v.getDecisionTier())) {
            decisionBadgeLabel.setForeground(DarkTheme.ACCENT_HOVER);
        } else if ("CAUTION".equals(v.getDecisionTier())) {
            decisionBadgeLabel.setForeground(DarkTheme.WARNING);
        } else {
            decisionBadgeLabel.setForeground(DarkTheme.DANGER);
        }

        valuationMetricLabel.setText(CURRENCY.format(v.getPostMoneyValuation()));
        founderEqMetricLabel.setText("Founders Retain: " + v.getFounderEquity() + "%");
        runwayMetricLabel.setText(v.getRunwayMonths() + " Months");
        burnMetricLabel.setText("Net Burn: " + CURRENCY.format(v.getMonthlyBurn()) + " / mo");
    }

    private void testAivenConnectionAsync() {
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() {
                try {
                    return DatabaseConnection.testConnection();
                } catch (Exception e) {
                    return -1L;
                }
            }

            @Override
            protected void done() {
                try {
                    long latency = get();
                    if (latency >= 0) {
                        dbStatusLabel.setText("CONNECTED (SSL REQUIRED, " + latency + "ms latency)");
                        dbStatusLabel.setForeground(DarkTheme.SUCCESS);
                    } else {
                        dbStatusLabel.setText("Offline / Local Engine Active (" + DatabaseConnection.getHost() + ")");
                        dbStatusLabel.setForeground(DarkTheme.WARNING);
                    }
                } catch (Exception ignored) {
                    dbStatusLabel.setText("Local Persistence Mode Active");
                    dbStatusLabel.setForeground(DarkTheme.TEXT_MUTED);
                }
            }
        }.execute();
    }

    @Override
    public void onUserLoggedIn(com.venturelens.model.User user) {
        reloadVenturesFromDB();
    }

    @Override
    public void onUserLoggedOut() {
        ventureComboBox.removeAllItems();
    }

    @Override
    public void onActiveVentureChanged(Venture venture) {
        if (venture != null && ventureComboBox.getSelectedItem() != venture) {
            ventureComboBox.setSelectedItem(venture);
            updateDashboard(venture);
        }
    }
}
