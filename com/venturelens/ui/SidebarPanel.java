package com.venturelens.ui;

import com.venturelens.model.User;
import com.venturelens.model.Venture;
import com.venturelens.ui.components.DarkTheme;
import com.venturelens.utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Left Navigation Sidebar with quick tab switching across all VentureLens modules.
 */
public class SidebarPanel extends JPanel implements UserSession.SessionListener {

    private final MainFrame mainFrame;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String activeScreen = MainFrame.SCREEN_OVERVIEW;

    private JLabel userLabel;
    private JLabel activeVentureLabel;

    public SidebarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(DarkTheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(240, 720));
        setBorder(new CompoundBorder(
                new LineBorder(DarkTheme.BORDER_COLOR, 1, false),
                new EmptyBorder(20, 16, 20, 16)
        ));

        initUI();
        UserSession.getInstance().addListener(this);
    }

    private void initUI() {
        // Top: Branding
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JLabel brandTitle = new JLabel("VentureLens SDSS");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandTitle.setForeground(DarkTheme.ACCENT);

        JLabel brandSubtitle = new JLabel("CS304 Java Swing + MySQL Project");
        brandSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSubtitle.setForeground(DarkTheme.TEXT_MUTED);

        topPanel.add(brandTitle);
        topPanel.add(Box.createVerticalStrut(2));
        topPanel.add(brandSubtitle);
        topPanel.add(Box.createVerticalStrut(16));

        add(topPanel, BorderLayout.NORTH);

        // Center: Navigation Items
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        addNavButton(navPanel, "📊 Dashboard Overview", MainFrame.SCREEN_OVERVIEW);
        addNavButton(navPanel, "💡 1. Idea Validator", MainFrame.SCREEN_VALIDATOR);
        addNavButton(navPanel, "📈 2. CapTable Simulator", MainFrame.SCREEN_CAPTABLE);
        addNavButton(navPanel, "🔥 3. BurnWatch Ledger", MainFrame.SCREEN_BURNWATCH);
        addNavButton(navPanel, "📋 4. PitchCraft Export", MainFrame.SCREEN_PITCHCRAFT);
        addNavButton(navPanel, "🗄️ 5. Database Records", MainFrame.SCREEN_HISTORY);

        add(navPanel, BorderLayout.CENTER);

        // Bottom: User Profile & Database Status
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Session & DB", 0, 0, DarkTheme.FONT_SMALL, DarkTheme.TEXT_MUTED),
                new EmptyBorder(6, 8, 8, 8)
        ));

        userLabel = new JLabel("Student: Gowtham S");
        userLabel.setFont(DarkTheme.FONT_SUBHEAD);
        userLabel.setForeground(DarkTheme.TEXT_PRIMARY);

        activeVentureLabel = new JLabel("Active: None");
        activeVentureLabel.setFont(DarkTheme.FONT_SMALL);
        activeVentureLabel.setForeground(DarkTheme.TEXT_MUTED);

        JLabel dbStatusLabel = new JLabel("● Aiven MySQL: Port 28491 (SSL)");
        dbStatusLabel.setFont(DarkTheme.FONT_SMALL);
        dbStatusLabel.setForeground(DarkTheme.SUCCESS);

        JButton logoutBtn = DarkTheme.createButton("Log Out", false);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        logoutBtn.addActionListener(e -> {
            UserSession.getInstance().logout();
            mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
        });

        bottomPanel.add(userLabel);
        bottomPanel.add(Box.createVerticalStrut(2));
        bottomPanel.add(activeVentureLabel);
        bottomPanel.add(Box.createVerticalStrut(2));
        bottomPanel.add(dbStatusLabel);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addNavButton(JPanel container, String text, String screenKey) {
        JButton btn = new JButton(text);
        btn.setFont(DarkTheme.FONT_SUBHEAD);
        btn.setForeground(DarkTheme.TEXT_MUTED);
        btn.setBackground(DarkTheme.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));

        btn.addActionListener(e -> {
            setActiveScreen(screenKey);
            mainFrame.showScreen(screenKey);
        });

        navButtons.put(screenKey, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    public void setActiveScreen(String screenKey) {
        this.activeScreen = screenKey;
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(screenKey)) {
                btn.setForeground(DarkTheme.TEXT_PRIMARY);
                btn.setFont(DarkTheme.FONT_BOLD);
                btn.setBackground(DarkTheme.CARD_BG);
                btn.setContentAreaFilled(true);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(DarkTheme.ACCENT, 1, true),
                        new EmptyBorder(7, 11, 7, 11)
                ));
            } else {
                btn.setForeground(DarkTheme.TEXT_MUTED);
                btn.setFont(DarkTheme.FONT_SUBHEAD);
                btn.setBackground(DarkTheme.SIDEBAR_BG);
                btn.setContentAreaFilled(false);
                btn.setBorder(new EmptyBorder(8, 12, 8, 12));
            }
        }
        repaint();
    }

    @Override
    public void onUserLoggedIn(User user) {
        if (user != null) {
            userLabel.setText(user.getFullName());
        }
    }

    @Override
    public void onUserLoggedOut() {
        userLabel.setText("Not Logged In");
        activeVentureLabel.setText("Active: None");
    }

    @Override
    public void onActiveVentureChanged(Venture venture) {
        if (venture != null) {
            activeVentureLabel.setText("Active: " + venture.getStartupName());
        } else {
            activeVentureLabel.setText("Active: None");
        }
    }
}
