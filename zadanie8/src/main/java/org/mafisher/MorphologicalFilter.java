package org.mafisher;

import java.awt.image.BufferedImage;

public class MorphologicalFilter {

    private int[][] structuringElement;
    private int seWidth;
    private int seHeight;
    private int anchorX;
    private int anchorY;

    public MorphologicalFilter(int[][] structuringElement) {
        this.structuringElement = structuringElement;
        this.seHeight = structuringElement.length;
        this.seWidth = structuringElement[0].length;
        this.anchorX = seWidth / 2;
        this.anchorY = seHeight / 2;
    }

    private boolean[][] toBinary(BufferedImage img, int threshold) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean[][] binary = new boolean[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int gray = ((rgb >> 16) & 0xFF + (rgb >> 8) & 0xFF + (rgb & 0xFF)) / 3;
                binary[y][x] = gray >= threshold;
            }
        }
        return binary;
    }

    private BufferedImage toImage(boolean[][] binary) {
        int h = binary.length;
        int w = binary[0].length;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int val = binary[y][x] ? 0xFFFFFF : 0x000000;
                img.setRGB(x, y, val);
            }
        }
        return img;
    }

    public BufferedImage dilate(BufferedImage img, int threshold) {
        boolean[][] binary = toBinary(img, threshold);
        int h = binary.length;
        int w = binary[0].length;
        boolean[][] result = new boolean[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean hitWhite = false;

                for (int j = 0; j < seHeight; j++) {
                    for (int i = 0; i < seWidth; i++) {
                        if (structuringElement[j][i] == 1) {
                            int ny = y + j - anchorY;
                            int nx = x + i - anchorX;

                            if (ny >= 0 && ny < h && nx >= 0 && nx < w) {
                                if (binary[ny][nx]) {
                                    hitWhite = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (hitWhite) break;
                }
                result[y][x] = hitWhite;
            }
        }
        return toImage(result);
    }

    public BufferedImage erode(BufferedImage img, int threshold) {
        boolean[][] binary = toBinary(img, threshold);
        int h = binary.length;
        int w = binary[0].length;
        boolean[][] result = new boolean[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean allMatch = true;

                for (int j = 0; j < seHeight; j++) {
                    for (int i = 0; i < seWidth; i++) {
                        if (structuringElement[j][i] == 1) {
                            int ny = y + j - anchorY;
                            int nx = x + i - anchorX;

                            if (ny < 0 || ny >= h || nx < 0 || nx >= w || !binary[ny][nx]) {
                                allMatch = false;
                                break;
                            }
                        }
                    }
                    if (!allMatch) break;
                }
                result[y][x] = allMatch;
            }
        }
        return toImage(result);
    }

    public BufferedImage open(BufferedImage img, int threshold) {
        BufferedImage eroded = erode(img, threshold);
        return dilate(eroded, 128);
    }

    public BufferedImage close(BufferedImage img, int threshold) {
        BufferedImage dilated = dilate(img, threshold);
        return erode(dilated, 128);
    }

    public BufferedImage hitOrMiss(BufferedImage img, int threshold, int[][] hitPattern, int[][] missPattern) {
        boolean[][] binary = toBinary(img, threshold);
        int h = binary.length;
        int w = binary[0].length;
        boolean[][] result = new boolean[h][w];

        int hitHeight = hitPattern.length;
        int hitWidth = hitPattern[0].length;
        int missHeight = missPattern.length;
        int missWidth = missPattern[0].length;
        int hitAnchorY = hitHeight / 2;
        int hitAnchorX = hitWidth / 2;
        int missAnchorY = missHeight / 2;
        int missAnchorX = missWidth / 2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean hitMatch = true;
                boolean missMatch = true;

                for (int j = 0; j < hitHeight && hitMatch; j++) {
                    for (int i = 0; i < hitWidth && hitMatch; i++) {
                        if (hitPattern[j][i] == 1) {
                            int ny = y + j - hitAnchorY;
                            int nx = x + i - hitAnchorX;

                            if (ny < 0 || ny >= h || nx < 0 || nx >= w || !binary[ny][nx]) {
                                hitMatch = false;
                            }
                        }
                    }
                }

                for (int j = 0; j < missHeight && missMatch; j++) {
                    for (int i = 0; i < missWidth && missMatch; i++) {
                        if (missPattern[j][i] == 1) {
                            int ny = y + j - missAnchorY;
                            int nx = x + i - missAnchorX;

                            if (ny >= 0 && ny < h && nx >= 0 && nx < w && binary[ny][nx]) {
                                missMatch = false;
                            }
                        }
                    }
                }

                result[y][x] = hitMatch && missMatch;
            }
        }
        return toImage(result);
    }

    public BufferedImage thin(BufferedImage img, int threshold) {
        int[][] hit = {
                {0, 0, 0},
                {-1, 1, -1},
                {1, 1, 1}
        };
        int[][] miss = {
                {1, 1, 1},
                {0, 0, 0},
                {0, 0, 0}
        };

        int[][] seHit = new int[3][3];
        int[][] seMiss = new int[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                seHit[i][j] = (hit[i][j] == 1) ? 1 : 0;
                seMiss[i][j] = (miss[i][j] == 1) ? 1 : 0;
            }
        }

        BufferedImage result = hitOrMiss(img, threshold, seHit, seMiss);

        boolean[][] binary = toBinary(img, threshold);
        boolean[][] hom = toBinary(result, 128);
        int h = binary.length;
        int w = binary[0].length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (hom[y][x]) {
                    binary[y][x] = false;
                }
            }
        }

        return toImage(binary);
    }

    public BufferedImage thicken(BufferedImage img, int threshold) {
        int[][] hit = {
                {1, 1, 1},
                {-1, 0, -1},
                {0, 0, 0}
        };
        int[][] miss = {
                {0, 0, 0},
                {1, 0, 1},
                {1, 1, 1}
        };

        int[][] seHit = new int[3][3];
        int[][] seMiss = new int[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                seHit[i][j] = (hit[i][j] == 1) ? 1 : 0;
                seMiss[i][j] = (miss[i][j] == 1) ? 1 : 0;
            }
        }

        BufferedImage result = hitOrMiss(img, threshold, seHit, seMiss);

        boolean[][] binary = toBinary(img, threshold);
        boolean[][] hom = toBinary(result, 128);
        int h = binary.length;
        int w = binary[0].length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (hom[y][x]) {
                    binary[y][x] = true;
                }
            }
        }

        return toImage(binary);
    }
}