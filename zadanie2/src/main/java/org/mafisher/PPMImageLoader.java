package org.mafisher;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PPMImageLoader implements ImageLoader {

    @Override
    public BufferedImage load(File file, boolean linearScale) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            bis.mark(64);
            byte[] magic = new byte[2];
            int r = bis.read(magic);
            if (r != 2 || magic[0] != 'P' || (magic[1] != '3' && magic[1] != '6')) return null;
            bis.reset();

            String magicNumber = readToken(bis);
            int width = Integer.parseInt(readToken(bis));
            int height = Integer.parseInt(readToken(bis));
            int maxval = Integer.parseInt(readToken(bis));

            PPMImage ppm = new PPMImage(width, height);

            if ("P3".equals(magicNumber)) {
                PPMTextReader reader = new PPMTextReader(bis, maxval, linearScale);
                reader.readPixels(ppm);
            } else {
                PPMBinaryReader reader = new PPMBinaryReader(bis, maxval, linearScale);
                reader.readPixels(ppm);
            }

            return ppm.toBufferedImage();
        }
    }

    private String readToken(BufferedInputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        boolean inToken = false;
        boolean comment = false;

        while ((c = in.read()) != -1) {
            char ch = (char)c;
            if (comment) {
                if (ch == '\n' || ch == '\r') comment = false;
                continue;
            }
            if (ch == '#') {
                comment = true;
                continue;
            }
            if (Character.isWhitespace(ch)) {
                if (inToken) break;
            } else {
                inToken = true;
                sb.append(ch);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
