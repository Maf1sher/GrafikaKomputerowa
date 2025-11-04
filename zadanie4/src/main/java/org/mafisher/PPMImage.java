package org.mafisher;

import java.awt.image.BufferedImage;

public class PPMImage {

    private final int width;
    private final int height;
    private final int[] pixels;

    public PPMImage(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public int[] getPixels() { return pixels; }

    public void setPixel(int x, int y, int r, int g, int b) {
        pixels[y * width + x] = (r << 16) | (g << 8) | b;
    }

    public BufferedImage toBufferedImage() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, width, height, pixels, 0, width);
        return img;
    }
}