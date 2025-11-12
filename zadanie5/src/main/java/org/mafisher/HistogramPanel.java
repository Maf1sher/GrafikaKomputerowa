package org.mafisher;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class HistogramPanel extends JPanel {
    private int[] histogramR;
    private int[] histogramG;
    private int[] histogramB;
    private int maxCount = 1;

    public HistogramPanel() {
        setPreferredSize(new Dimension(280, 200));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Histogram"));
    }

    public void updateHistogram(BufferedImage image) {
        if (image == null) {
            histogramR = histogramG = histogramB = null;
            repaint();
            return;
        }

        histogramR = new int[256];
        histogramG = new int[256];
        histogramB = new int[256];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                histogramR[r]++;
                histogramG[g]++;
                histogramB[b]++;
            }
        }

        maxCount = 1;
        for (int i = 0; i < 256; i++) {
            maxCount = Math.max(maxCount, histogramR[i]);
            maxCount = Math.max(maxCount, histogramG[i]);
            maxCount = Math.max(maxCount, histogramB[i]);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        if (histogramR == null) {
            g.setColor(Color.GRAY);
            g.drawString("Brak histogramu", 10, getHeight() / 2);
            return;
        }

        int w = getWidth() - 10;
        int h = getHeight() - 30;
        double scaleX = w / 256.0;
        double scaleY = h / (double) maxCount;

        g.translate(5, 5);

        g.setColor(new Color(255, 0, 0, 80));
        for (int i = 0; i < 256; i++) {
            int barHeight = (int) (histogramR[i] * scaleY);
            g.fillRect((int) (i * scaleX), h - barHeight, Math.max(1, (int) Math.ceil(scaleX)), barHeight);
        }

        g.setColor(new Color(0, 255, 0, 80));
        for (int i = 0; i < 256; i++) {
            int barHeight = (int) (histogramG[i] * scaleY);
            g.fillRect((int) (i * scaleX), h - barHeight, Math.max(1, (int) Math.ceil(scaleX)), barHeight);
        }

        g.setColor(new Color(0, 0, 255, 80));
        for (int i = 0; i < 256; i++) {
            int barHeight = (int) (histogramB[i] * scaleY);
            g.fillRect((int) (i * scaleX), h - barHeight, Math.max(1, (int) Math.ceil(scaleX)), barHeight);
        }

        g.setColor(Color.BLACK);
        g.drawRect(0, 0, w, h);

        g.setColor(Color.GRAY);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g.drawString("0", 2, h + 12);
        g.drawString("128", w / 2 - 8, h + 12);
        g.drawString("255", w - 15, h + 12);
    }
}