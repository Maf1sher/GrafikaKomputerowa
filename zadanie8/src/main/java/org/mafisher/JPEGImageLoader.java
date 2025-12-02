package org.mafisher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JPEGImageLoader implements ImageLoader {

    @Override
    public BufferedImage load(File file, boolean linearScale) throws IOException {
        return ImageIO.read(file);
    }
}