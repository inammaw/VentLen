package com.venturelens.ui;

import com.venturelens.dao.VentureDAO;
import com.venturelens.model.Venture;
import com.venturelens.ui.components.DarkTheme;
import com.venturelens.ui.components.DecisionBadgeRenderer;
import com.venturelens.utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Module 5: Saved Plans, History & Archives.
 */
public class HistoryPanel extends JPanel implements UserSession.SessionListener {

    private final MainFrame mainFrame;
    private final VentureDAO ventureDAO = new VentureDAO();
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private JTable venturesTable;
    private VentureHistoryTableModel tableModel;

    // Inspection Card fields
    private JLabel inspectTitleLabel;
    private JLabel inspectTierLabel;
    private JTextArea inspectProblemArea;
    private JTextArea inspectSolutionArea;
    private JLabel inspectValuationLabel;
    private JLabel inspectRunwayLabel;
    private JLabel statusLabel;

    public HistoryPanel(MainFrame mainFrame) {
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

        JLabel title = new JLabel("Saved Evaluations & History Archive");
        title.setFont(DarkTheme.FONT_TITLE);
        title.setForeground(DarkTheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Relational database repository of evaluated ventures with custom badge cell renderers");
        sub.setFont(DarkTheme.FONT_BODY);
        sub.setForeground(DarkTheme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        // Header Actions
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton refreshBtn = DarkTheme.createButton("Refresh Archive", false);
        refreshBtn.addActionListener(e -> reloadFromDB());
        btns.add(refreshBtn);

        header.add(btns, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center: Split with Table on Top/Left and Inspection Details on Bottom
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Table Card
        JPanel tableCard = DarkTheme.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.setPreferredSize(new Dimension(Integer.MAX_VALUE, 260));

        tableModel = new VentureHistoryTableModel();
        venturesTable = new JTable(tableModel);
        DarkTheme.styleTable(venturesTable);

        // Set custom DecisionBadgeRenderer on the Decision Tier column (col index 2)
        venturesTable.getColumnModel().getColumn(2).setCellRenderer(new DecisionBadgeRenderer());
        venturesTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        venturesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = venturesTable.getSelectedRow();
                if (row >= 0) {
                    Venture v = tableModel.getVenture(row);
                    updateInspectionCard(v);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(venturesTable);
        scroll.getViewport().setBackground(DarkTheme.CARD_BG);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableCard.add(scroll, BorderLayout.CENTER);

        center.add(tableCard);
        center.add(Box.createVerticalStrut(16));

        // Bottom Inspection Card
        JPanel inspectCard = DarkTheme.createCard();
        inspectCard.setLayout(new BorderLayout());
        inspectCard.setBorder(new CompoundBorder(
                new LineBorder(DarkTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Inspection Header
        JPanel insHead = new JPanel(new BorderLayout());
        insHead.setOpaque(false);

        inspectTitleLabel = new JLabel("Select an evaluation to inspect details");
        inspectTitleLabel.setFont(DarkTheme.FONT_HEADER);
        inspectTitleLabel.setForeground(DarkTheme.TEXT_PRIMARY);
        insHead.add(inspectTitleLabel, BorderLayout.WEST);

        inspectTierLabel = new JLabel("");
        inspectTierLabel.setFont(DarkTheme.FONT_BOLD);
        insHead.add(inspectTierLabel, BorderLayout.EAST);

        inspectCard.add(insHead, BorderLayout.NORTH);

        // Inspection Details Grid
        JPanel insGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        insGrid.setOpaque(false);
        insGrid.setBorder(new EmptyBorder(12, 0, 12, 0));

        // Left box
        JPanel leftBox = new JPanel();
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.Y_AXIS));
        leftBox.setOpaque(false);

        JLabel pLbl = new JLabel("Problem Statement:");
        pLbl.setFont(DarkTheme.FONT_SMALL);
        pLbl.setForeground(DarkTheme.TEXT_MUTED);
        inspectProblemArea = DarkTheme.createTextArea(3, 20);
        inspectProblemArea.setEditable(false);
        inspectProblemArea.setOpaque(false);

        leftBox.add(pLbl);
        leftBox.add(Box.createVerticalStrut(4));
        leftBox.add(inspectProblemArea);

        insGrid.add(leftBox);

        // Right box
        JPanel rightBox = new JPanel();
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));
        rightBox.setOpaque(false);

        JLabel sLbl = new JLabel("Solution & Value Prop:");
        sLbl.setFont(DarkTheme.FONT_SMALL);
        sLbl.setForeground(DarkTheme.TEXT_MUTED);
        inspectSolutionArea = DarkTheme.createTextArea(3, 20);
        inspectSolutionArea.setEditable(false);
        inspectSolutionArea.setOpaque(false);

        rightBox.add(sLbl);
        rightBox.add(Box.createVerticalStrut(4));
        rightBox.add(inspectSolutionArea);

        insGrid.add(rightBox);
        inspectCard.add(insGrid, BorderLayout.CENTER);

        // Inspection Footer Actions
        JPanel insFoot = new JPanel(new BorderLayout());
        insFoot.setOpaque(false);

        JPanel metrics = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        metrics.setOpaque(false);

        inspectValuationLabel = new JLabel("Post-Money: —");
        inspectValuationLabel.setFont(DarkTheme.FONT_SUBHEAD);
        inspectValuationLabel.setForeground(DarkTheme.TEXT_PRIMARY);

        inspectRunwayLabel = new JLabel("Runway: —");
        inspectRunwayLabel.setFont(DarkTheme.FONT_SUBHEAD);
        inspectRunwayLabel.setForeground(DarkTheme.SUCCESS);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(DarkTheme.FONT_SMALL);
        statusLabel.setForeground(DarkTheme.TEXT_MUTED);

        metrics.add(inspectValuationLabel);
        metrics.add(inspectRunwayLabel);
        metrics.add(statusLabel);

        insFoot.add(metrics, BorderLayout.WEST);

        JPanel actBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actBtns.setOpaque(false);

        JButton selectActiveBtn = DarkTheme.createButton("Set as Active Venture", true);
        selectActiveBtn.addActionListener(e -> selectCurrentAsActive());
        actBtns.add(selectActiveBtn);

        JButton deleteBtn = DarkTheme.createButton("Delete", false);
        deleteBtn.addActionListener(e -> deleteSelectedVenture());
        actBtns.add(deleteBtn);

        insFoot.add(actBtns, BorderLayout.EAST);
        inspectCard.add(insFoot, BorderLayout.SOUTH);

        center.add(inspectCard);

        add(center, BorderLayout.CENTER);

        reloadFromDB();
    }

    private void reloadFromDB() {
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
                    tableModel.setVentures(list);
                    if (!list.isEmpty()) {
                        venturesTable.setRowSelectionInterval(0, 0);
                        updateInspectionCard(list.get(0));
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Database sync note: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateInspectionCard(Venture v) {
        if (v == null) return;
        inspectTitleLabel.setText(v.getStartupName() + " — " + v.getTargetCustomer());
        inspectTierLabel.setText(v.getDecisionTier() + " (" + v.getOverallScore() + "/100)");

        if ("STRONG_GO".equals(v.getDecisionTier())) {
            inspectTierLabel.setForeground(DarkTheme.SUCCESS);
        } else if ("GO".equals(v.getDecisionTier())) {
            inspectTierLabel.setForeground(DarkTheme.ACCENT_HOVER);
        } else if ("CAUTION".equals(v.getDecisionTier())) {
            inspectTierLabel.setForeground(DarkTheme.WARNING);
        } else {
            inspectTierLabel.setForeground(DarkTheme.DANGER);
        }

        inspectProblemArea.setText(v.getProblemStatement());
        inspectSolutionArea.setText(v.getProposedSolution());
        inspectValuationLabel.setText("Valuation: " + CURRENCY.format(v.getPostMoneyValuation()));
        inspectRunwayLabel.setText("Runway: " + v.getRunwayMonths() + " Mos");
    }

    private void selectCurrentAsActive() {
        int row = venturesTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Please select a venture row first.");
            return;
        }
        Venture v = tableModel.getVenture(row);
        UserSession.getInstance().setActiveVenture(v);
        statusLabel.setText("Activated '" + v.getStartupName() + "' as active session venture!");
        mainFrame.showScreen(MainFrame.SCREEN_OVERVIEW);
    }

    private void deleteSelectedVenture() {
        int row = venturesTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Please select a venture row first.");
            return;
        }
        Venture v = tableModel.getVenture(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + v.getStartupName() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return ventureDAO.deleteById(v.getId());
                }

                @Override
                protected void done() {
                    tableModel.removeRow(row);
                    statusLabel.setText("Deleted venture.");
                }
            }.execute();
        }
    }

    @Override
    public void onUserLoggedIn(com.venturelens.model.User user) {
        reloadFromDB();
    }

    @Override
    public void onUserLoggedOut() {
        tableModel.setVentures(new ArrayList<>());
    }

    @Override
    public void onActiveVentureChanged(Venture venture) {}

    // Custom TableModel for History
    private static class VentureHistoryTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Startup Name", "Decision Tier", "Composite Score", "Valuation", "Runway"};
        private final List<Venture> ventures = new ArrayList<>();

        public void setVentures(List<Venture> list) {
            ventures.clear();
            if (list != null) ventures.addAll(list);
            fireTableDataChanged();
        }

        public void removeRow(int row) {
            if (row >= 0 && row < ventures.size()) {
                ventures.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }

        public Venture getVenture(int row) {
            return ventures.get(row);
        }

        @Override
        public int getRowCount() { return ventures.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Venture v = ventures.get(row);
            switch (col) {
                case 0: return "#" + v.getId();
                case 1: return v.getStartupName();
                case 2: return v.getDecisionTier();
                case 3: return v.getOverallScore() + " / 100";
                case 4: return CURRENCY.format(v.getPostMoneyValuation());
                case 5: return v.getRunwayMonths() + " mos";
                default: return "";
            }
        }
    }
}
