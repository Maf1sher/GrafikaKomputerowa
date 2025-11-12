package org.mafisher;

import java.awt.image.BufferedImage;

public class HistogramOperations {

    public static BufferedImage stretchHistogram(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int minR = 255, maxR = 0;
        int minG = 255, maxG = 0;
        int minB = 255, maxB = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                minR = Math.min(minR, r);
                maxR = Math.max(maxR, r);
                minG = Math.min(minG, g);
                maxG = Math.max(maxG, g);
                minB = Math.min(minB, b);
                maxB = Math.max(maxB, b);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int newR = maxR > minR ? (r - minR) * 255 / (maxR - minR) : r;
                int newG = maxG > minG ? (g - minG) * 255 / (maxG - minG) : g;
                int newB = maxB > minB ? (b - minB) * 255 / (maxB - minB) : b;

                out.setRGB(x, y, (newR << 16) | (newG << 8) | newB);
            }
        }

        return out;
    }

    public static BufferedImage equalizeHistogram(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int totalPixels = width * height;
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[] histR = new int[256];
        int[] histG = new int[256];
        int[] histB = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                histR[(rgb >> 16) & 0xFF]++;
                histG[(rgb >> 8) & 0xFF]++;
                histB[rgb & 0xFF]++;
            }
        }

        int[] cdfR = new int[256];
        int[] cdfG = new int[256];
        int[] cdfB = new int[256];

        cdfR[0] = histR[0];
        cdfG[0] = histG[0];
        cdfB[0] = histB[0];

        for (int i = 1; i < 256; i++) {
            cdfR[i] = cdfR[i - 1] + histR[i];
            cdfG[i] = cdfG[i - 1] + histG[i];
            cdfB[i] = cdfB[i - 1] + histB[i];
        }

        int[] lutR = new int[256];
        int[] lutG = new int[256];
        int[] lutB = new int[256];

        for (int i = 0; i < 256; i++) {
            lutR[i] = (cdfR[i] * 255) / totalPixels;
            lutG[i] = (cdfG[i] * 255) / totalPixels;
            lutB[i] = (cdfB[i] * 255) / totalPixels;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int newR = lutR[r];
                int newG = lutG[g];
                int newB = lutB[b];

                out.setRGB(x, y, (newR << 16) | (newG << 8) | newB);
            }
        }

        return out;
    }
}