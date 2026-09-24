package com.venturelens.ui;

import com.venturelens.dao.VentureDAO;
import com.venturelens.model.Venture;
import com.venturelens.ui.components.DarkTheme;
import com.venturelens.ui.components.DonutChartPanel;
import com.venturelens.utils.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Module 2: CapTable & Equity Dilution Simulator.
 * Financial precision via java.math.BigDecimal and pure Java Graphics2D Donut Chart.
 */
public class CapTablePanel extends JPanel implements UserSession.SessionListener {

    private final MainFrame mainFrame;
    private final VentureDAO ventureDAO = new VentureDAO();
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    // Inputs
    private JTextField preMoneyField;
    private JTextField investmentField;
    private JSlider esopSlider;
    private JLabel esopValueLabel;
    private JSlider founderSlider;
    private JLabel founderValueLabel;

    // Outputs
    private JLabel postMoneyLabel;
    private JLabel investorPctLabel;
    private JLabel dilutedFounderLabel;
    private JLabel finalEsopLabel;
    private DonutChartPanel donutChart;

    private JButton saveBtn;
    private JLabel statusLabel;

    public CapTablePanel(MainFrame mainFrame) {
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

        JLabel title = new JLabel("CapTable & Equity Dilution Simulator");
        title.setFont(DarkTheme.FONT_TITLE);
        title.setForeground(DarkTheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("High-precision BigDecimal dilution modeling with native Graphics2D vector donut rendering");
        sub.setFont(DarkTheme.FONT_BODY);
        sub.setForeground(DarkTheme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Center Split: Left = Controls, Right = Chart & Metrics
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);
        centerGrid.setBorder(new EmptyBorder(16, 0, 0, 0));

        // LEFT: Financial Inputs Card
        JPanel inputCard = DarkTheme.createCard();
        inputCard.setLayout(new BoxLayout(inputCard, BoxLayout.Y_AXIS));

        JLabel inTitle = new JLabel("Investment Round Parameters");
        inTitle.setFont(DarkTheme.FONT_HEADER);
        inTitle.setForeground(DarkTheme.TEXT_PRIMARY);
        inputCard.add(inTitle);
        inputCard.add(Box.createVerticalStrut(14));

        // Pre-Money
        inputCard.add(createInputLabel("Pre-Money Valuation (₹ INR)"));
        inputCard.add(Box.createVerticalStrut(4));
        preMoneyField = DarkTheme.createTextField(15);
        preMoneyField.setText("8000000");
        inputCard.add(preMoneyField);
        inputCard.add(Box.createVerticalStrut(12));

        // Investment Ask
        inputCard.add(createInputLabel("Investment Round Amount (₹ INR)"));
        inputCard.add(Box.createVerticalStrut(4));
        investmentField = DarkTheme.createTextField(15);
        investmentField.setText("1500000");
        inputCard.add(investmentField);
        inputCard.add(Box.createVerticalStrut(14));

        // ESOP Pool Slider
        JPanel esopHeader = new JPanel(new BorderLayout());
        esopHeader.setOpaque(false);
        esopHeader.add(createInputLabel("Reserved ESOP Talent Pool"), BorderLayout.WEST);
        esopValueLabel = new JLabel("10.0%");
        esopValueLabel.setFont(DarkTheme.FONT_BOLD);
        esopValueLabel.setForeground(DarkTheme.SUCCESS);
        esopHeader.add(esopValueLabel, BorderLayout.EAST);
        inputCard.add(esopHeader);
        inputCard.add(Box.createVerticalStrut(4));

        esopSlider = new JSlider(0, 30, 10);
        esopSlider.setBackground(DarkTheme.CARD_BG);
        esopSlider.setForeground(DarkTheme.TEXT_MUTED);
        esopSlider.addChangeListener(e -> {
            esopValueLabel.setText(esopSlider.getValue() + ".0%");
            calculateDilution();
        });
        inputCard.add(esopSlider);
        inputCard.add(Box.createVerticalStrut(14));

        // Actions
        JPanel actRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actRow.setOpaque(false);

        JButton calcBtn = DarkTheme.createButton("Recalculate Dilution", true);
        calcBtn.addActionListener(e -> calculateDilution());
        actRow.add(calcBtn);

        saveBtn = DarkTheme.createButton("Save CapTable to DB", false);
        saveBtn.addActionListener(e -> saveCapTableToDB());
        actRow.add(saveBtn);

        inputCard.add(actRow);
        inputCard.add(Box.createVerticalStrut(12));

        statusLabel = new JLabel("Adjust inputs and click 'Recalculate Dilution'");
        statusLabel.setFont(DarkTheme.FONT_SMALL);
        statusLabel.setForeground(DarkTheme.TEXT_MUTED);
        inputCard.add(statusLabel);

        centerGrid.add(inputCard);

        // RIGHT: Chart & Post-Money Metrics Card
        JPanel chartCard = DarkTheme.createCard();
        chartCard.setLayout(new BorderLayout());

        JPanel metricHeader = new JPanel(new GridLayout(1, 2, 10, 0));
        metricHeader.setOpaque(false);

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.setOpaque(false);
        JLabel pmTitle = new JLabel("POST-MONEY VALUATION");
        pmTitle.setFont(DarkTheme.FONT_SMALL);
        pmTitle.setForeground(DarkTheme.TEXT_MUTED);
        postMoneyLabel = new JLabel("₹95,00,000");
        postMoneyLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        postMoneyLabel.setForeground(DarkTheme.TEXT_PRIMARY);
        p1.add(pmTitle);
        p1.add(postMoneyLabel);
        metricHeader.add(p1);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.setOpaque(false);
        JLabel inTitle2 = new JLabel("NEW INVESTOR EQUITY");
        inTitle2.setFont(DarkTheme.FONT_SMALL);
        inTitle2.setForeground(DarkTheme.TEXT_MUTED);
        investorPctLabel = new JLabel("15.8%");
        investorPctLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        investorPctLabel.setForeground(new Color(0xD9, 0x77, 0x06));
        p2.add(inTitle2);
        p2.add(investorPctLabel);
        metricHeader.add(p2);

        chartCard.add(metricHeader, BorderLayout.NORTH);

        // Custom Donut Chart
        donutChart = new DonutChartPanel();
        chartCard.add(donutChart, BorderLayout.CENTER);

        // Bottom Breakdown
        JPanel breakdown = new JPanel(new GridLayout(1, 3, 10, 0));
        breakdown.setOpaque(false);
        breakdown.setBorder(new EmptyBorder(10, 0, 0, 0));

        dilutedFounderLabel = new JLabel("Founders: 74.2%");
        dilutedFounderLabel.setFont(DarkTheme.FONT_SUBHEAD);
        dilutedFounderLabel.setForeground(DarkTheme.ACCENT_HOVER);

        finalEsopLabel = new JLabel("ESOP Pool: 10.0%");
        finalEsopLabel.setFont(DarkTheme.FONT_SUBHEAD);
        finalEsopLabel.setForeground(DarkTheme.SUCCESS);

        breakdown.add(dilutedFounderLabel);
        breakdown.add(finalEsopLabel);
        chartCard.add(breakdown, BorderLayout.SOUTH);

        centerGrid.add(chartCard);
        add(centerGrid, BorderLayout.CENTER);

        calculateDilution();
    }

    private JLabel createInputLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(DarkTheme.FONT_SUBHEAD);
        l.setForeground(DarkTheme.TEXT_PRIMARY);
        return l;
    }

    private void calculateDilution() {
        try {
            BigDecimal preMoney = new BigDecimal(preMoneyField.getText().trim());
            BigDecimal investment = new BigDecimal(investmentField.getText().trim());
            BigDecimal esopPct = new BigDecimal(esopSlider.getValue());

            // Post Money = Pre Money + Investment
            BigDecimal postMoney = preMoney.add(investment);
            postMoneyLabel.setText(CURRENCY.format(postMoney));

            // Investor Ownership % = (Investment / Post Money) * 100
            BigDecimal investorPct = investment.divide(postMoney, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);
            investorPctLabel.setText(investorPct.toPlainString() + "%");

            // Diluted Founder % = 100 - Investor% - ESOP%
            BigDecimal founderPct = new BigDecimal("100.0").subtract(investorPct).subtract(esopPct)
                    .setScale(1, RoundingMode.HALF_UP);

            if (founderPct.compareTo(BigDecimal.ZERO) < 0) {
                founderPct = BigDecimal.ZERO;
            }

            dilutedFounderLabel.setText("Founders: " + founderPct.toPlainString() + "%");
            finalEsopLabel.setText("ESOP Pool: " + esopPct.toPlainString() + "%");

            // Update Donut Chart
            donutChart.setData(founderPct, esopPct, investorPct);
            statusLabel.setText("Dilution updated: Post-money " + CURRENCY.format(postMoney));
        } catch (Exception ex) {
            statusLabel.setText("Please enter valid numeric amounts for valuations.");
        }
    }

    private void saveCapTableToDB() {
        Venture active = UserSession.getInstance().getActiveVenture();
        if (active == null) {
            statusLabel.setText("Please select or save a venture in Overview first.");
            return;
        }

        try {
            BigDecimal preMoney = new BigDecimal(preMoneyField.getText().trim());
            BigDecimal investment = new BigDecimal(investmentField.getText().trim());
            BigDecimal esopPct = new BigDecimal(esopSlider.getValue());
            BigDecimal postMoney = preMoney.add(investment);
            BigDecimal investorPct = investment.divide(postMoney, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal founderPct = new BigDecimal("100.00").subtract(investorPct).subtract(esopPct)
                    .setScale(2, RoundingMode.HALF_UP);

            active.setPreMoneyValuation(preMoney);
            active.setInvestmentAmount(investment);
            active.setPostMoneyValuation(postMoney);
            active.setEsopPool(esopPct);
            active.setInvestorEquity(investorPct);
            active.setFounderEquity(founderPct);

            saveBtn.setEnabled(false);
            statusLabel.setText("Saving CapTable snapshot to MySQL...");

            new SwingWorker<Venture, Void>() {
                @Override
                protected Venture doInBackground() throws Exception {
                    return ventureDAO.update(active);
                }

                @Override
                protected void done() {
                    saveBtn.setEnabled(true);
                    statusLabel.setText("CapTable successfully saved to database for " + active.getStartupName());
                }
            }.execute();
        } catch (Exception ex) {
            statusLabel.setText("Error saving: " + ex.getMessage());
        }
    }

    @Override
    public void onUserLoggedIn(com.venturelens.model.User user) {}

    @Override
    public void onUserLoggedOut() {}

    @Override
    public void onActiveVentureChanged(Venture venture) {
        if (venture != null) {
            preMoneyField.setText(venture.getPreMoneyValuation().toPlainString());
            investmentField.setText(venture.getInvestmentAmount().toPlainString());
            esopSlider.setValue(venture.getEsopPool().intValue());
            calculateDilution();
        }
    }
}
