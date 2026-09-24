package com.venturelens;

import com.venturelens.ui.MainFrame;
import com.venturelens.utils.DatabaseConnection;

import javax.swing.*;

/**
 * VentureLens — Startup Operating System & Decision Intelligence Suite.
 * Pure Java Swing Desktop Application with JDBC, Aiven MySQL, Rule-based NLP,
 * CapTable Dilution Modeling, BurnWatch, and PitchCraft.
 */
public class Main {

    public static void main(String[] args) {
        // Configure text antialiasing for crisp typography across all Swing components
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Load Aiven Cloud database configuration
        DatabaseConnection.loadProperties();

        // Launch UI strictly on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply standard system look-and-feel (strictly no third-party L&F libraries)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default cross-platform Metal without crashing
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
