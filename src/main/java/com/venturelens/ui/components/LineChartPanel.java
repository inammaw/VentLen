package com.venturelens.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Custom Line Chart for BurnWatch Cash Depletion & Runway Projection.
 * Pure Java 2D Graphics using Graphics2D, Antialiasing, gradients, and coordinate transforms.
 * Strictly no third-party charting libraries.
 */
public class LineChartPanel extends JPanel {

    private double startCash = 1200000.0;
    private double monthlyBurn = 68000.0;
    private int projectedMonths = 18;

    private static final NumberFormat CURRENCY_SHORT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public LineChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(480, 240));
    }

    public void setData(double startCash, double monthlyBurn) {
        this.startCash = Math.max(0, startCash);
        this.monthlyBurn = Math.max(1000, monthlyBurn);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int padLeft = 60;
        int padRight = 30;
        int padTop = 30;
        int padBottom = 40;

        int plotW = w - padLeft - padRight;
        int plotH = h - padTop - padBottom;

        if (plotW <= 50 || plotH <= 50) {
            g2.dispose();
            return;
        }

        // Calculate points
        List<Point> points = new ArrayList<>();
        double currentCash = startCash;
        double maxCash = Math.max(startCash * 1.1, 100000.0);

        for (int m = 0; m <= projectedMonths; m++) {
            double xPct = (double) m / (double) projectedMonths;
            double yPct = Math.max(0.0, currentCash) / maxCash;

            int px = padLeft + (int) (xPct * plotW);
            int py = padTop + (int) ((1.0 - yPct) * plotH);
            points.add(new Point(px, py));

            currentCash -= monthlyBurn;
        }

        // Draw horizontal grid lines & labels
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(DarkTheme.BORDER_COLOR);
        Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f}, 0.0f);
        Stroke solid = new BasicStroke(1.5f);

        int gridSteps = 4;
        for (int i = 0; i <= gridSteps; i++) {
            int gy = padTop + (int) ((double) i / gridSteps * plotH);
            double val = maxCash * (1.0 - (double) i / gridSteps);

            g2.setStroke(dashed);
            g2.setColor(new Color(0xDE, 0xE2, 0xE6));
            g2.drawLine(padLeft, gy, padLeft + plotW, gy);

            g2.setColor(DarkTheme.TEXT_MUTED);
            String label = formatCurrencyShort(val);
            g2.drawString(label, padLeft - 52, gy + 4);
        }

        // Draw Zero-Cash baseline
        int zeroY = padTop + plotH;
        g2.setColor(new Color(0xDC, 0x26, 0x26, 120)); // Red warning line
        g2.drawLine(padLeft, zeroY, padLeft + plotW, zeroY);

        // Fill area under curve
        if (points.size() > 1) {
            GeneralPath path = new GeneralPath();
            path.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size(); i++) {
                path.lineTo(points.get(i).x, points.get(i).y);
            }
            path.lineTo(points.get(points.size() - 1).x, zeroY);
            path.lineTo(points.get(0).x, zeroY);
            path.closePath();

            GradientPaint gp = new GradientPaint(
                    0, padTop, new Color(0x1B, 0x6E, 0xC2, 60),
                    0, zeroY, new Color(0x1B, 0x6E, 0xC2, 5)
            );
            g2.setPaint(gp);
            g2.fill(path);
        }

        // Draw the main line
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(DarkTheme.ACCENT);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Draw node circles & month markers
        for (int m = 0; m <= projectedMonths; m++) {
            Point p = points.get(m);

            // Draw dot on milestone months
            if (m % 3 == 0 || m == projectedMonths) {
                g2.setColor(DarkTheme.CARD_BG);
                g2.fillOval(p.x - 5, p.y - 5, 10, 10);
                g2.setColor(m == 0 ? DarkTheme.SUCCESS : (p.y >= zeroY ? DarkTheme.DANGER : DarkTheme.ACCENT));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(p.x - 5, p.y - 5, 10, 10);

                // Month text below X axis
                g2.setColor(DarkTheme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String mText = "M" + m;
                int textW = g2.getFontMetrics().stringWidth(mText);
                g2.drawString(mText, p.x - textW / 2, zeroY + 16);
            }
        }

        g2.dispose();
    }

    private String formatCurrencyShort(double val) {
        if (val >= 10000000) {
            return String.format("₹%.1f Cr", val / 10000000.0);
        } else if (val >= 100000) {
            return String.format("₹%.1f L", val / 100000.0);
        } else if (val >= 1000) {
            return String.format("₹%.0fk", val / 1000.0);
        } else {
            return String.format("₹%.0f", val);
        }
    }
}
