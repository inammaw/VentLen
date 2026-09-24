package com.venturelens.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom TableCellRenderer rendering clean light-themed badges for Venture decision tiers.
 * Designed for standard Java SE 17+ Swing.
 */
public class DecisionBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        String tier = value != null ? value.toString() : "";

        Color bg;
        Color fg;
        Color border;

        switch (tier) {
            case "STRONG_GO":
                bg = new Color(0xD1, 0xE7, 0xDD); // Soft mint/green
                fg = new Color(0x0F, 0x51, 0x32); // Dark green
                border = new Color(0xBA, 0xDF, 0xC8);
                break;
            case "GO":
                bg = new Color(0xCF, 0xE2, 0xFF); // Soft sky blue
                fg = new Color(0x08, 0x42, 0x98); // Dark blue
                border = new Color(0xB6, 0xD4, 0xFE);
                break;
            case "CAUTION":
                bg = new Color(0xFF, 0xF3, 0xCD); // Soft warm yellow
                fg = new Color(0x66, 0x4D, 0x03); // Dark amber
                border = new Color(0xFF, 0xEE, 0xBA);
                break;
            case "PIVOT":
            default:
                bg = new Color(0xF8, 0xD7, 0xDA); // Soft pink/red
                fg = new Color(0x84, 0x20, 0x29); // Dark red
                border = new Color(0xF5, 0xC2, 0xC7);
                break;
        }

        JLabel label = new JLabel(tier, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth() - 16;
                int h = getHeight() - 8;
                g2.setColor(bg);
                g2.fillRoundRect(8, 4, w, h, 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(8, 4, w, h, 8, 8);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        label.setOpaque(false);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(fg);

        return label;
    }
}
