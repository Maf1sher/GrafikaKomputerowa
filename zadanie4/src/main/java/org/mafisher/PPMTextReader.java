package org.mafisher;

import java.io.BufferedInputStream;
import java.io.IOException;

public class PPMTextReader {

    private final BufferedInputStream bis;
    private final int maxval;
    private final boolean linearScale;

    public PPMTextReader(BufferedInputStream bis, int maxval, boolean linearScale) {
        this.bis = bis;
        this.maxval = maxval;
        this.linearScale = linearScale;
    }

    public void readPixels(PPMImage img) throws IOException {
        int width = img.getWidth();
        int height = img.getHeight();
        int total = width * height * 3;
        int[] pixels = img.getPixels();
        StringBuilder sb = new StringBuilder();
        int idx = 0;

        int b;
        while (idx < total && (b = bis.read()) != -1) {
            char ch = (char)b;
            if (ch == '#') {
                while ((b = bis.read()) != -1 && b != '\n' && b != '\r');
                continue;
            }
            if (Character.isWhitespace(ch)) {
                if (sb.length() > 0) {
                    int val = Integer.parseInt(sb.toString());
                    sb.setLength(0);

                    if (linearScale) val = val * 255 / maxval;

                    int mod = idx % 3;
                    if (mod == 0) pixels[idx / 3] = val << 16;
                    else if (mod == 1) pixels[idx / 3] |= val << 8;
                    else pixels[idx / 3] |= val;

                    idx++;
                }
            } else sb.append(ch);
        }

        if (idx < total) throw new IOException("Za mało danych w P3");
    }
}