package com.venturelens.ui;

import com.venturelens.dao.UserDAO;
import com.venturelens.model.User;
import com.venturelens.ui.components.DarkTheme;
import com.venturelens.utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Login Screen featuring SHA-256 password validation and SwingWorker async DB access.
 */
public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserDAO userDAO = new UserDAO();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginBtn;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(DarkTheme.BG_DARK);

        initUI();
    }

    private void initUI() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(DarkTheme.CARD_BG);
        card.setPreferredSize(new Dimension(380, 440));
        card.setBorder(new CompoundBorder(
                new LineBorder(DarkTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(32, 32, 32, 32)
        ));

        JLabel title = new JLabel("Sign In to VentureLens");
        title.setFont(DarkTheme.FONT_TITLE);
        title.setForeground(DarkTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Decision Intelligence & Startup OS");
        subtitle.setFont(DarkTheme.FONT_BODY);
        subtitle.setForeground(DarkTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));

        // Username
        JLabel uLabel = new JLabel("Username");
        uLabel.setFont(DarkTheme.FONT_SUBHEAD);
        uLabel.setForeground(DarkTheme.TEXT_PRIMARY);
        uLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField = DarkTheme.createTextField(20);
        usernameField.setText("founder");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        card.add(uLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));

        // Password
        JLabel pLabel = new JLabel("Password");
        pLabel.setFont(DarkTheme.FONT_SUBHEAD);
        pLabel.setForeground(DarkTheme.TEXT_PRIMARY);
        pLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = DarkTheme.createPasswordField(20);
        passwordField.setText("admin123");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        card.add(pLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(DarkTheme.FONT_SMALL);
        statusLabel.setForeground(DarkTheme.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(12));

        // Login Button
        loginBtn = DarkTheme.createButton("Sign In", true);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        card.add(loginBtn);
        card.add(Box.createVerticalStrut(14));

        // Demo credentials link
        JButton registerLink = new JButton("Don't have an account? Create one");
        registerLink.setFont(DarkTheme.FONT_SMALL);
        registerLink.setForeground(DarkTheme.ACCENT_HOVER);
        registerLink.setContentAreaFilled(false);
        registerLink.setBorderPainted(false);
        registerLink.setFocusPainted(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_REGISTER));

        card.add(registerLink);

        add(card);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        loginBtn.setEnabled(false);
        statusLabel.setForeground(DarkTheme.TEXT_MUTED);
        statusLabel.setText("Authenticating with MySQL...");

        // Run authentication on background thread via SwingWorker to avoid freezing EDT
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return userDAO.authenticate(username, password);
            }

            @Override
            protected void done() {
                loginBtn.setEnabled(true);
                try {
                    User user = get();
                    if (user != null) {
                        statusLabel.setText(" ");
                        UserSession.getInstance().setCurrentUser(user);
                        mainFrame.onLoginSuccess();
                    } else {
                        statusLabel.setForeground(DarkTheme.DANGER);
                        statusLabel.setText("Invalid username or password.");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(DarkTheme.DANGER);
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
