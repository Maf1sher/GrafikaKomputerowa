package org.mafisher;

import java.awt.image.BufferedImage;

public class BinarizationOperations {

    public static BufferedImage binarizeManual(BufferedImage src, int threshold) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;

                int newValue = gray >= threshold ? 255 : 0;
                out.setRGB(x, y, (newValue << 16) | (newValue << 8) | newValue);
            }
        }

        return out;
    }

    public static BufferedImage binarizePercentBlack(BufferedImage src, double percentBlack) {
        int width = src.getWidth();
        int height = src.getHeight();
        int totalPixels = width * height;

        int[] histogram = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                histogram[gray]++;
            }
        }

        int targetBlackPixels = (int) (totalPixels * percentBlack / 100.0);
        int sum = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            sum += histogram[i];
            if (sum >= targetBlackPixels) {
                threshold = i;
                break;
            }
        }

        return binarizeManual(src, threshold);
    }

    public static BufferedImage binarizeMeanIterative(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int totalPixels = width * height;

        int[] grayValues = new int[totalPixels];
        int idx = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                grayValues[idx++] = (r + g + b) / 3;
            }
        }

        int sum = 0;
        for (int v : grayValues) sum += v;
        int threshold = sum / totalPixels;

        for (int iter = 0; iter < 100; iter++) {
            int sum1 = 0, count1 = 0;
            int sum2 = 0, count2 = 0;

            for (int v : grayValues) {
                if (v < threshold) {
                    sum1 += v;
                    count1++;
                } else {
                    sum2 += v;
                    count2++;
                }
            }

            int mean1 = count1 > 0 ? sum1 / count1 : 0;
            int mean2 = count2 > 0 ? sum2 / count2 : 255;
            int newThreshold = (mean1 + mean2) / 2;

            if (newThreshold == threshold) break;
            threshold = newThreshold;
        }

        return binarizeManual(src, threshold);
    }
}