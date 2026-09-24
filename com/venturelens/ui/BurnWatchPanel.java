package com.venturelens.ui;

import com.venturelens.dao.ExpenseDAO;
import com.venturelens.dao.VentureDAO;
import com.venturelens.model.ExpenseEntry;
import com.venturelens.model.Venture;
import com.venturelens.ui.components.DarkTheme;
import com.venturelens.ui.components.LineChartPanel;
import com.venturelens.utils.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Module 3: BurnWatch Cash Flow, Runway, and Zero-Cash Date Tracker.
 */
public class BurnWatchPanel extends JPanel implements UserSession.SessionListener {

    private final MainFrame mainFrame;
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final VentureDAO ventureDAO = new VentureDAO();
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private JTextField cashBalanceField;
    private JLabel netBurnLabel;
    private JLabel runwayMonthsLabel;
    private JLabel zeroCashDateLabel;

    private JTable ledgerTable;
    private LedgerTableModel tableModel;
    private LineChartPanel lineChart;

    // Add entry fields
    private JComboBox<String> categoryCombo;
    private JTextField descField;
    private JTextField amountField;
    private JCheckBox isRevenueCheck;
    private JLabel statusLabel;

    public BurnWatchPanel(MainFrame mainFrame) {
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

        JLabel title = new JLabel("BurnWatch — Cash Flow & Survival Tracker");
        title.setFont(DarkTheme.FONT_TITLE);
        title.setForeground(DarkTheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Dynamic ledger, Net Burn rate calculation, and predictive zero-cash depletion curves");
        sub.setFont(DarkTheme.FONT_BODY);
        sub.setForeground(DarkTheme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Center split: Top Metric Cards + Split View (Ledger Table on Left, Line Chart on Right)
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Metric Cards Row
        JPanel metricRow = new JPanel(new GridLayout(1, 4, 12, 0));
        metricRow.setOpaque(false);
        metricRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        metricRow.add(createMetricCard("CURRENT CASH BALANCE", cashBalanceField = DarkTheme.createTextField(10)));
        cashBalanceField.setText("1200000");
        cashBalanceField.addActionListener(e -> recalculateMetrics());

        metricRow.add(createReadonlyMetricCard("NET MONTHLY BURN", netBurnLabel = new JLabel("₹68,000 / mo")));
        metricRow.add(createReadonlyMetricCard("SURVIVAL RUNWAY", runwayMonthsLabel = new JLabel("17.6 Months"), DarkTheme.SUCCESS));
        metricRow.add(createReadonlyMetricCard("ZERO-CASH CLIFF", zeroCashDateLabel = new JLabel("Aug 2027"), DarkTheme.WARNING));

        content.add(metricRow);
        content.add(Box.createVerticalStrut(16));

        // Main Lower Grid
        JPanel splitGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        splitGrid.setOpaque(false);

        // LEFT: Ledger Table Card & Add Form
        JPanel tableCard = DarkTheme.createCard();
        tableCard.setLayout(new BorderLayout());

        JLabel tTitle = new JLabel("Operating Ledger & Cash Inflow/Outflow");
        tTitle.setFont(DarkTheme.FONT_HEADER);
        tTitle.setForeground(DarkTheme.TEXT_PRIMARY);
        tableCard.add(tTitle, BorderLayout.NORTH);

        tableModel = new LedgerTableModel();
        ledgerTable = new JTable(tableModel);
        DarkTheme.styleTable(ledgerTable);

        JScrollPane scroll = new JScrollPane(ledgerTable);
        scroll.getViewport().setBackground(DarkTheme.CARD_BG);
        scroll.setBorder(new EmptyBorder(12, 0, 12, 0));
        tableCard.add(scroll, BorderLayout.CENTER);

        // Add Item Form at bottom of table
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addPanel.setOpaque(false);

        categoryCombo = new JComboBox<>(new String[]{"SALARY", "HOSTING_INFRA", "MARKETING", "LEGAL_ADMIN", "REVENUE", "OTHER"});
        categoryCombo.setBackground(DarkTheme.INPUT_BG);
        categoryCombo.setForeground(DarkTheme.TEXT_PRIMARY);
        categoryCombo.setFont(DarkTheme.FONT_SMALL);

        descField = DarkTheme.createTextField(10);
        descField.setText("AWS Servers");

        amountField = DarkTheme.createTextField(6);
        amountField.setText("4500");

        isRevenueCheck = new JCheckBox("Revenue");
        isRevenueCheck.setForeground(DarkTheme.TEXT_MUTED);
        isRevenueCheck.setOpaque(false);

        JButton addBtn = DarkTheme.createButton("Add", true);
        addBtn.addActionListener(e -> addLedgerEntry());

        JButton delBtn = DarkTheme.createButton("Delete Selected", false);
        delBtn.addActionListener(e -> deleteSelectedEntry());

        addPanel.add(categoryCombo);
        addPanel.add(descField);
        addPanel.add(amountField);
        addPanel.add(isRevenueCheck);
        addPanel.add(addBtn);
        addPanel.add(delBtn);

        tableCard.add(addPanel, BorderLayout.SOUTH);
        splitGrid.add(tableCard);

        // RIGHT: Line Chart Card
        JPanel chartCard = DarkTheme.createCard();
        chartCard.setLayout(new BorderLayout());

        JPanel chartHead = new JPanel(new BorderLayout());
        chartHead.setOpaque(false);

        JLabel cTitle = new JLabel("Projected Cash Depletion Curve");
        cTitle.setFont(DarkTheme.FONT_HEADER);
        cTitle.setForeground(DarkTheme.TEXT_PRIMARY);
        chartHead.add(cTitle, BorderLayout.WEST);

        JButton saveSnapBtn = DarkTheme.createButton("Save Runway to DB", false);
        saveSnapBtn.addActionListener(e -> saveRunwayToDB());
        chartHead.add(saveSnapBtn, BorderLayout.EAST);

        chartCard.add(chartHead, BorderLayout.NORTH);

        lineChart = new LineChartPanel();
        chartCard.add(lineChart, BorderLayout.CENTER);

        statusLabel = new JLabel("Runway projection calculated from ledger items and cash balance");
        statusLabel.setFont(DarkTheme.FONT_SMALL);
        statusLabel.setForeground(DarkTheme.TEXT_MUTED);
        chartCard.add(statusLabel, BorderLayout.SOUTH);

        splitGrid.add(chartCard);
        content.add(splitGrid);

        add(content, BorderLayout.CENTER);

        loadLedgerFromDB();
    }

    private JPanel createMetricCard(String title, JComponent comp) {
        JPanel card = DarkTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(title);
        l.setFont(DarkTheme.FONT_SMALL);
        l.setForeground(DarkTheme.TEXT_MUTED);
        card.add(l);
        card.add(Box.createVerticalStrut(6));
        comp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(comp);
        return card;
    }

    private JPanel createReadonlyMetricCard(String title, JLabel comp, Color color) {
        JPanel card = DarkTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(title);
        l.setFont(DarkTheme.FONT_SMALL);
        l.setForeground(DarkTheme.TEXT_MUTED);
        card.add(l);
        card.add(Box.createVerticalStrut(6));
        comp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        comp.setForeground(color);
        card.add(comp);
        return card;
    }

    private JPanel createReadonlyMetricCard(String title, JLabel comp) {
        return createReadonlyMetricCard(title, comp, DarkTheme.TEXT_PRIMARY);
    }

    private void recalculateMetrics() {
        double totalExpense = 0;
        double totalRevenue = 0;

        for (ExpenseEntry e : tableModel.entries) {
            if (e.isRevenue()) {
                totalRevenue += e.getAmount().doubleValue();
            } else {
                totalExpense += e.getAmount().doubleValue();
            }
        }

        double netBurn = totalExpense - totalRevenue;
        if (netBurn <= 0) netBurn = 1000.0; // Break-even / profitable floor

        double cashBalance = 1200000.0;
        try {
            cashBalance = Double.parseDouble(cashBalanceField.getText().trim());
        } catch (Exception ignored) {}

        double runwayMonths = cashBalance / netBurn;

        netBurnLabel.setText(CURRENCY.format(netBurn) + " / mo");
        runwayMonthsLabel.setText(String.format("%.1f Months", runwayMonths));

        if (runwayMonths >= 12.0) {
            runwayMonthsLabel.setForeground(DarkTheme.SUCCESS);
        } else if (runwayMonths >= 6.0) {
            runwayMonthsLabel.setForeground(DarkTheme.WARNING);
        } else {
            runwayMonthsLabel.setForeground(DarkTheme.DANGER);
        }

        // Estimate Zero-Cash Date
        int monthsToAdd = (int) Math.round(runwayMonths);
        LocalDate zeroDate = LocalDate.now().plusMonths(monthsToAdd);
        zeroCashDateLabel.setText(zeroDate.format(DateTimeFormatter.ofPattern("MMM yyyy")));

        // Update line chart
        lineChart.setData(cashBalance, netBurn);
    }

    private void addLedgerEntry() {
        try {
            String cat = (String) categoryCombo.getSelectedItem();
            String desc = descField.getText().trim();
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            boolean rev = isRevenueCheck.isSelected();

            if (desc.isEmpty()) desc = "Operating cost";

            ExpenseEntry entry = new ExpenseEntry(cat, desc, amount, rev, "2026-03");
            entry.setVentureId(getActiveVentureId());

            new SwingWorker<ExpenseEntry, Void>() {
                @Override
                protected ExpenseEntry doInBackground() throws Exception {
                    return expenseDAO.insert(entry);
                }

                @Override
                protected void done() {
                    try {
                        ExpenseEntry saved = get();
                        tableModel.addEntry(saved);
                        recalculateMetrics();
                        statusLabel.setText("Added ledger entry: " + saved.getDescription());
                    } catch (Exception ex) {
                        tableModel.addEntry(entry);
                        recalculateMetrics();
                    }
                }
            }.execute();
        } catch (Exception ex) {
            statusLabel.setText("Please enter valid numeric amount.");
        }
    }

    private void deleteSelectedEntry() {
        int row = ledgerTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a row in the ledger to delete.");
            return;
        }

        ExpenseEntry e = tableModel.getEntry(row);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return expenseDAO.delete(e.getId(), e.getVentureId());
            }

            @Override
            protected void done() {
                tableModel.removeRow(row);
                recalculateMetrics();
                statusLabel.setText("Removed ledger entry.");
            }
        }.execute();
    }

    private void loadLedgerFromDB() {
        int vId = getActiveVentureId();
        new SwingWorker<List<ExpenseEntry>, Void>() {
            @Override
            protected List<ExpenseEntry> doInBackground() throws Exception {
                return expenseDAO.findByVentureId(vId);
            }

            @Override
            protected void done() {
                try {
                    List<ExpenseEntry> list = get();
                    tableModel.setEntries(list);
                    recalculateMetrics();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void saveRunwayToDB() {
        Venture v = UserSession.getInstance().getActiveVenture();
        if (v == null) {
            statusLabel.setText("Please select an active venture in Overview.");
            return;
        }

        try {
            double cash = Double.parseDouble(cashBalanceField.getText().trim());
            double totalExp = 0;
            double totalRev = 0;
            for (ExpenseEntry e : tableModel.entries) {
                if (e.isRevenue()) totalRev += e.getAmount().doubleValue();
                else totalExp += e.getAmount().doubleValue();
            }
            double netBurn = Math.max(1000.0, totalExp - totalRev);
            double runway = cash / netBurn;

            v.setCurrentCashBalance(BigDecimal.valueOf(cash));
            v.setMonthlyBurn(BigDecimal.valueOf(netBurn));
            v.setRunwayMonths(BigDecimal.valueOf(runway).setScale(1, RoundingMode.HALF_UP));

            new SwingWorker<Venture, Void>() {
                @Override
                protected Venture doInBackground() throws Exception {
                    return ventureDAO.update(v);
                }

                @Override
                protected void done() {
                    statusLabel.setText("Saved cash runway snapshot to MySQL database.");
                }
            }.execute();
        } catch (Exception ex) {
            statusLabel.setText("Error saving runway: " + ex.getMessage());
        }
    }

    private int getActiveVentureId() {
        Venture v = UserSession.getInstance().getActiveVenture();
        return v != null ? v.getId() : 1;
    }

    @Override
    public void onUserLoggedIn(com.venturelens.model.User user) {
        loadLedgerFromDB();
    }

    @Override
    public void onUserLoggedOut() {}

    @Override
    public void onActiveVentureChanged(Venture venture) {
        if (venture != null) {
            cashBalanceField.setText(venture.getCurrentCashBalance().toPlainString());
            loadLedgerFromDB();
        }
    }

    // Custom TableModel for Ledger
    private static class LedgerTableModel extends AbstractTableModel {
        private final String[] columns = {"Category", "Description", "Type", "Amount"};
        private final List<ExpenseEntry> entries = new ArrayList<>();

        public void setEntries(List<ExpenseEntry> list) {
            this.entries.clear();
            if (list != null) this.entries.addAll(list);
            fireTableDataChanged();
        }

        public void addEntry(ExpenseEntry e) {
            this.entries.add(e);
            fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
        }

        public void removeRow(int row) {
            if (row >= 0 && row < entries.size()) {
                entries.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }

        public ExpenseEntry getEntry(int row) {
            return entries.get(row);
        }

        @Override
        public int getRowCount() { return entries.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            ExpenseEntry e = entries.get(row);
            switch (col) {
                case 0: return e.getCategory();
                case 1: return e.getDescription();
                case 2: return e.isRevenue() ? "REVENUE" : "EXPENSE";
                case 3: return CURRENCY.format(e.getAmount());
                default: return "";
            }
        }
    }
}
