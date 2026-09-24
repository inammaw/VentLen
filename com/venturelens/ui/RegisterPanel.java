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
 * Registration Screen with SHA-256 password hashing and async database creation.
 */
public class RegisterPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserDAO userDAO = new UserDAO();

    private JTextField fullNameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton registerBtn;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(DarkTheme.BG_DARK);

        initUI();
    }

    private void initUI() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(DarkTheme.CARD_BG);
        card.setPreferredSize(new Dimension(400, 520));
        card.setBorder(new CompoundBorder(
                new LineBorder(DarkTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(28, 32, 28, 32)
        ));

        JLabel title = new JLabel("Create Founder Account");
        title.setFont(DarkTheme.FONT_TITLE);
        title.setForeground(DarkTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(20));

        // Full Name
        card.add(createFieldLabel("Full Name"));
        card.add(Box.createVerticalStrut(4));
        fullNameField = DarkTheme.createTextField(20);
        fullNameField.setText("Jordan Lee");
        card.add(fullNameField);
        card.add(Box.createVerticalStrut(12));

        // Username
        card.add(createFieldLabel("Username"));
        card.add(Box.createVerticalStrut(4));
        usernameField = DarkTheme.createTextField(20);
        usernameField.setText("jordan");
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));

        // Email
        card.add(createFieldLabel("Email"));
        card.add(Box.createVerticalStrut(4));
        emailField = DarkTheme.createTextField(20);
        emailField.setText("jordan@startup.io");
        card.add(emailField);
        card.add(Box.createVerticalStrut(12));

        // Password
        card.add(createFieldLabel("Password (hashed with SHA-256)"));
        card.add(Box.createVerticalStrut(4));
        passwordField = DarkTheme.createPasswordField(20);
        passwordField.setText("founder2026");
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(DarkTheme.FONT_SMALL);
        statusLabel.setForeground(DarkTheme.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(12));

        // Register Button
        registerBtn = DarkTheme.createButton("Create Account", true);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> performRegister());

        card.add(registerBtn);
        card.add(Box.createVerticalStrut(14));

        JButton backLink = new JButton("Already have an account? Sign in");
        backLink.setFont(DarkTheme.FONT_SMALL);
        backLink.setForeground(DarkTheme.ACCENT_HOVER);
        backLink.setContentAreaFilled(false);
        backLink.setBorderPainted(false);
        backLink.setFocusPainted(false);
        backLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        backLink.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_LOGIN));

        card.add(backLink);

        add(card);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DarkTheme.FONT_SUBHEAD);
        label.setForeground(DarkTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void performRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        registerBtn.setEnabled(false);
        statusLabel.setForeground(DarkTheme.TEXT_MUTED);
        statusLabel.setText("Saving user to database...");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return userDAO.registerUser(username, email, password, fullName);
            }

            @Override
            protected void done() {
                registerBtn.setEnabled(true);
                try {
                    User user = get();
                    UserSession.getInstance().setCurrentUser(user);
                    mainFrame.onLoginSuccess();
                } catch (Exception ex) {
                    statusLabel.setForeground(DarkTheme.DANGER);
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
