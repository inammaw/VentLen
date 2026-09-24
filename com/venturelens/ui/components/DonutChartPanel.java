package com.venturelens.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Custom Donut Chart for CapTable Equity Distribution.
 * Pure Java 2D Graphics using Graphics2D, Antialiasing, and Arc2D/Ellipse2D.
 * Strictly no third-party charting libraries like JFreeChart.
 */
public class DonutChartPanel extends JPanel {

    private BigDecimal founderEquity = new BigDecimal("75.0");
    private BigDecimal esopPool = new BigDecimal("10.0");
    private BigDecimal investorEquity = new BigDecimal("15.0");

    // Colors matching Student Light Theme
    private static final Color COLOR_FOUNDER = new Color(0x1B, 0x6E, 0xC2);   // Classic Blue #1B6EC2
    private static final Color COLOR_ESOP = new Color(0x1A, 0x7F, 0x37);      // Forest Green #1A7F37
    private static final Color COLOR_INVESTOR = new Color(0xD9, 0x77, 0x06);  // Amber Gold #D97706

    public DonutChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(320, 260));
    }

    public void setData(BigDecimal founder, BigDecimal esop, BigDecimal investor) {
        this.founderEquity = founder != null ? founder : BigDecimal.ZERO;
        this.esopPool = esop != null ? esop : BigDecimal.ZERO;
        this.investorEquity = investor != null ? investor : BigDecimal.ZERO;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Layout: Donut on left, Legend on right
        int chartSize = Math.min(width - 150, height - 30);
        if (chartSize < 80) chartSize = 80;

        int chartX = 20;
        int chartY = (height - chartSize) / 2;

        double total = founderEquity.doubleValue() + esopPool.doubleValue() + investorEquity.doubleValue();
        if (total <= 0) total = 100.0;

        double founderAngle = (founderEquity.doubleValue() / total) * 360.0;
        double esopAngle = (esopPool.doubleValue() / total) * 360.0;
        double investorAngle = 360.0 - (founderAngle + esopAngle);

        // Draw Arcs
        double currentAngle = 90.0;

        // 1. Founder slice
        g2.setColor(COLOR_FOUNDER);
        g2.fill(new Arc2D.Double(chartX, chartY, chartSize, chartSize, currentAngle, -founderAngle, Arc2D.PIE));
        currentAngle -= founderAngle;

        // 2. ESOP slice
        g2.setColor(COLOR_ESOP);
        g2.fill(new Arc2D.Double(chartX, chartY, chartSize, chartSize, currentAngle, -esopAngle, Arc2D.PIE));
        currentAngle -= esopAngle;

        // 3. Investor slice
        g2.setColor(COLOR_INVESTOR);
        g2.fill(new Arc2D.Double(chartX, chartY, chartSize, chartSize, currentAngle, -investorAngle, Arc2D.PIE));

        // Cut out center to create Donut
        int holeSize = (int) (chartSize * 0.58);
        int holeX = chartX + (chartSize - holeSize) / 2;
        int holeY = chartY + (chartSize - holeSize) / 2;

        g2.setColor(DarkTheme.CARD_BG);
        g2.fill(new Ellipse2D.Double(holeX, holeY, holeSize, holeSize));
        g2.setColor(DarkTheme.BORDER_COLOR);
        g2.draw(new Ellipse2D.Double(holeX, holeY, holeSize, holeSize));

        // Center text: Total Equity 100%
        g2.setColor(DarkTheme.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        String centerVal = "100%";
        g2.drawString(centerVal, holeX + (holeSize - fm.stringWidth(centerVal)) / 2, holeY + holeSize / 2 - 2);

        g2.setColor(DarkTheme.TEXT_MUTED);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        fm = g2.getFontMetrics();
        String centerSub = "CAPTABLE";
        g2.drawString(centerSub, holeX + (holeSize - fm.stringWidth(centerSub)) / 2, holeY + holeSize / 2 + 14);

        // Draw Legend on Right
        int legendX = chartX + chartSize + 24;
        int legendY = chartY + 16;
        int rowHeight = 44;

        drawLegendItem(g2, legendX, legendY, COLOR_FOUNDER, "Founders", founderEquity);
        drawLegendItem(g2, legendX, legendY + rowHeight, COLOR_ESOP, "ESOP Pool", esopPool);
        drawLegendItem(g2, legendX, legendY + rowHeight * 2, COLOR_INVESTOR, "Investors", investorEquity);

        g2.dispose();
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label, BigDecimal pct) {
        // Color pill
        g2.setColor(color);
        g2.fillRoundRect(x, y + 2, 12, 12, 4, 4);

        // Label
        g2.setColor(DarkTheme.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString(label, x + 20, y + 12);

        // Percentage
        g2.setColor(DarkTheme.TEXT_MUTED);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        String formattedPct = pct.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
        g2.drawString(formattedPct, x + 20, y + 26);
    }
}
