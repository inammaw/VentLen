package com.venturelens.ui.components;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * VentureLens Student Swing Theme (Clean Light Theme).
 * Designed for standard Java SE 17+ desktop Swing coursework.
 * Strictly uses standard javax.swing and java.awt without external Look-and-Feels.
 */
public final class DarkTheme {

    // Classic Java Swing Student Light Palette
    public static final Color BG_DARK = new Color(0xF0, 0xF2, 0xF5);       // Desktop window background #F0F2F5
    public static final Color CARD_BG = new Color(0xFF, 0xFF, 0xFF);       // Card/Panel background #FFFFFF
    public static final Color SIDEBAR_BG = new Color(0xE8, 0xEC, 0xF2);    // Navigation/Sidebar background #E8ECF2
    public static final Color INPUT_BG = new Color(0xFF, 0xFF, 0xFF);      // Text input background #FFFFFF
    public static final Color ACCENT = new Color(0x1B, 0x6E, 0xC2);        // Classic Java Student Blue #1B6EC2
    public static final Color ACCENT_HOVER = new Color(0x15, 0x58, 0x9C);  // Blue hover #15589C
    public static final Color TEXT_PRIMARY = new Color(0x21, 0x25, 0x29);  // Readable dark charcoal #212529
    public static final Color TEXT_MUTED = new Color(0x5A, 0x65, 0x78);    // Muted slate gray #5A6578
    public static final Color BORDER_COLOR = new Color(0xCC, 0xD4, 0xDC);  // Etched/line border #CCD4DC
    public static final Color SUCCESS = new Color(0x1A, 0x7F, 0x37);       // Forest green #1A7F37
    public static final Color WARNING = new Color(0x8A, 0x5B, 0x00);       // Warm amber #8A5B00
    public static final Color DANGER = new Color(0xCF, 0x22, 0x2E);        // Alert red #CF222E
    public static final Color INFO_BLUE = new Color(0x09, 0x69, 0xDA);     // Info blue #0969DA

    // Typography (Standard Academic Swing Hierarchy)
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private DarkTheme() {}

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        return card;
    }

    public static JPanel createTitledCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FONT_SUBHEAD,
                ACCENT
        );
        card.setBorder(new CompoundBorder(tb, new EmptyBorder(10, 12, 10, 12)));
        return card;
    }

    public static JButton createButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SUBHEAD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isPrimary) {
            btn.setBackground(ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(ACCENT_HOVER, 1, true),
                    new EmptyBorder(8, 16, 8, 16)
            ));
        } else {
            btn.setBackground(new Color(0xF4, 0xF6, 0xF9));
            btn.setForeground(TEXT_PRIMARY);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 16, 8, 16)
            ));
        }

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isPrimary) {
                    btn.setBackground(ACCENT_HOVER);
                } else {
                    btn.setBackground(new Color(0xE8, 0xEC, 0xF1));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (isPrimary) {
                    btn.setBackground(ACCENT);
                } else {
                    btn.setBackground(new Color(0xF4, 0xF6, 0xF9));
                }
            }
        });

        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setFont(FONT_BODY);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setBackground(INPUT_BG);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(TEXT_PRIMARY);
        pf.setFont(FONT_BODY);
        pf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return pf;
    }

    public static JTextArea createTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setBackground(INPUT_BG);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(TEXT_PRIMARY);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(6, 8, 6, 8));
        return ta;
    }

    public static void styleTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(0xDE, 0xE2, 0xE6));
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(0xCF, 0xE2, 0xFF)); // Windows/Swing soft selection blue
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0xEB, 0xEF, 0xF4));
        header.setForeground(TEXT_PRIMARY);
        header.setFont(FONT_SUBHEAD);
        header.setBorder(new LineBorder(BORDER_COLOR, 1));
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        table.setDefaultRenderer(Object.class, renderer);
    }
}
