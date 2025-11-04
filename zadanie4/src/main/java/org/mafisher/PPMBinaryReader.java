package org.mafisher;

import java.io.BufferedInputStream;
import java.io.IOException;

public class PPMBinaryReader {

    private final BufferedInputStream bis;
    private final int maxval;
    private final boolean linearScale;

    public PPMBinaryReader(BufferedInputStream bis, int maxval, boolean linearScale) {
        this.bis = bis;
        this.maxval = maxval;
        this.linearScale = linearScale;
    }

    public void readPixels(PPMImage img) throws IOException {
        skipWhitespace();
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = img.getPixels();
        int samples = width * height * 3;

        if (maxval < 256) {
            byte[] buffer = bis.readNBytes(samples);
            if (buffer.length < samples) throw new IOException("Za mało danych w P6");
            int p = 0;
            for (int i = 0; i < pixels.length; i++) {
                int r = buffer[p++] & 0xFF;
                int g = buffer[p++] & 0xFF;
                int b = buffer[p++] & 0xFF;

                if (linearScale) {
                    r = r * 255 / maxval;
                    g = g * 255 / maxval;
                    b = b * 255 / maxval;
                }

                pixels[i] = (r << 16) | (g << 8) | b;
            }
        } else {
            int pixelIndex = 0;
            int sampleIndex = 0;
            byte[] tmp = new byte[1 << 16];
            while (sampleIndex < samples) {
                int read = bis.read(tmp, 0, Math.min(tmp.length, samples*2 - sampleIndex*2));
                if (read == -1) break;
                int pos = 0;
                while (pos + 1 < read && sampleIndex < samples) {
                    int high = tmp[pos++] & 0xFF;
                    int low = tmp[pos++] & 0xFF;
                    int val = (high << 8 | low);

                    if (linearScale) val = val * 255 / maxval;

                    int mod = sampleIndex % 3;
                    if (mod == 0) pixels[pixelIndex] = val << 16;
                    else if (mod == 1) pixels[pixelIndex] |= val << 8;
                    else {
                        pixels[pixelIndex] |= val;
                        pixelIndex++;
                    }
                    sampleIndex++;
                }
            }
            if (sampleIndex < samples) throw new IOException("Za mało danych w P6 16-bit");
        }
    }

    private void skipWhitespace() throws IOException {
        int c;
        do {
            bis.mark(1);
            c = bis.read();
        } while (c != -1 && Character.isWhitespace(c));
        if (c != -1) bis.reset();
    }
}