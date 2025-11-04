package org.mafisher;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ImageProcessor {

    private static int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    public BufferedImage add(BufferedImage src, int valueR, int valueG, int valueB) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = ((rgb >> 16) & 0xFF) + valueR;
                int g = ((rgb >> 8) & 0xFF) + valueG;
                int b = (rgb & 0xFF) + valueB;
                result.setRGB(x, y, (clamp(r) << 16) | (clamp(g) << 8) | clamp(b));
            }
        }
        return result;
    }

    public BufferedImage subtract(BufferedImage src, int valueR, int valueG, int valueB) {
        return add(src, -valueR, -valueG, -valueB);
    }

    public BufferedImage multiply(BufferedImage src, double factorR, double factorG, double factorB) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (int)(((rgb >> 16) & 0xFF) * factorR);
                int g = (int)(((rgb >> 8) & 0xFF) * factorG);
                int b = (int)((rgb & 0xFF) * factorB);
                result.setRGB(x, y, (clamp(r) << 16) | (clamp(g) << 8) | clamp(b));
            }
        }
        return result;
    }

    public BufferedImage divide(BufferedImage src, double divisorR, double divisorG, double divisorB) {
        if (divisorR == 0 || divisorG == 0 || divisorB == 0) {
            throw new IllegalArgumentException("Dzielnik nie może być zerem");
        }
        return multiply(src, 1.0/divisorR, 1.0/divisorG, 1.0/divisorB);
    }

    public BufferedImage changeBrightness(BufferedImage src, int delta) {
        return add(src, delta, delta, delta);
    }

    public BufferedImage toGrayscaleAverage(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                result.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        return result;
    }

    public BufferedImage toGrayscaleLuminosity(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                result.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        return result;
    }

    public BufferedImage smoothingFilter(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int sumR = 0, sumG = 0, sumB = 0, count = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            int rgb = src.getRGB(nx, ny);
                            sumR += (rgb >> 16) & 0xFF;
                            sumG += (rgb >> 8) & 0xFF;
                            sumB += rgb & 0xFF;
                            count++;
                        }
                    }
                }

                int avgR = sumR / count;
                int avgG = sumG / count;
                int avgB = sumB / count;
                result.setRGB(x, y, (avgR << 16) | (avgG << 8) | avgB);
            }
        }
        return result;
    }

    public BufferedImage medianFilter(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int[] reds = new int[9];
                int[] greens = new int[9];
                int[] blues = new int[9];
                int idx = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = Math.max(0, Math.min(w - 1, x + dx));
                        int ny = Math.max(0, Math.min(h - 1, y + dy));
                        int rgb = src.getRGB(nx, ny);
                        reds[idx] = (rgb >> 16) & 0xFF;
                        greens[idx] = (rgb >> 8) & 0xFF;
                        blues[idx] = rgb & 0xFF;
                        idx++;
                    }
                }

                Arrays.sort(reds);
                Arrays.sort(greens);
                Arrays.sort(blues);

                result.setRGB(x, y, (reds[4] << 16) | (greens[4] << 8) | blues[4]);
            }
        }
        return result;
    }

    public BufferedImage sobelFilter(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int gxR = 0, gyR = 0, gxG = 0, gyG = 0, gxB = 0, gyB = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = Math.max(0, Math.min(w - 1, x + dx));
                        int ny = Math.max(0, Math.min(h - 1, y + dy));
                        int rgb = src.getRGB(nx, ny);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        int kx = sobelX[dy + 1][dx + 1];
                        int ky = sobelY[dy + 1][dx + 1];

                        gxR += kx * r;
                        gyR += ky * r;
                        gxG += kx * g;
                        gyG += ky * g;
                        gxB += kx * b;
                        gyB += ky * b;
                    }
                }

                int magR = (int)Math.sqrt(gxR * gxR + gyR * gyR);
                int magG = (int)Math.sqrt(gxG * gxG + gyG * gyG);
                int magB = (int)Math.sqrt(gxB * gxB + gyB * gyB);

                result.setRGB(x, y, (clamp(magR) << 16) | (clamp(magG) << 8) | clamp(magB));
            }
        }
        return result;
    }

    public BufferedImage sharpenFilter(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int[][] kernel = {{0, -1, 0}, {-1, 5, -1}, {0, -1, 0}};

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int sumR = 0, sumG = 0, sumB = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = Math.max(0, Math.min(w - 1, x + dx));
                        int ny = Math.max(0, Math.min(h - 1, y + dy));
                        int rgb = src.getRGB(nx, ny);
                        int k = kernel[dy + 1][dx + 1];

                        sumR += k * ((rgb >> 16) & 0xFF);
                        sumG += k * ((rgb >> 8) & 0xFF);
                        sumB += k * (rgb & 0xFF);
                    }
                }

                result.setRGB(x, y, (clamp(sumR) << 16) | (clamp(sumG) << 8) | clamp(sumB));
            }
        }
        return result;
    }

    public BufferedImage gaussianBlur(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        double[][] kernel = {
                {1.0/16, 2.0/16, 1.0/16},
                {2.0/16, 4.0/16, 2.0/16},
                {1.0/16, 2.0/16, 1.0/16}
        };

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double sumR = 0, sumG = 0, sumB = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = Math.max(0, Math.min(w - 1, x + dx));
                        int ny = Math.max(0, Math.min(h - 1, y + dy));
                        int rgb = src.getRGB(nx, ny);
                        double k = kernel[dy + 1][dx + 1];

                        sumR += k * ((rgb >> 16) & 0xFF);
                        sumG += k * ((rgb >> 8) & 0xFF);
                        sumB += k * (rgb & 0xFF);
                    }
                }

                result.setRGB(x, y, (clamp((int)sumR) << 16) | (clamp((int)sumG) << 8) | clamp((int)sumB));
            }
        }
        return result;
    }
}