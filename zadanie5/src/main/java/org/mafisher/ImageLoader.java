package org.mafisher;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface ImageLoader {
    BufferedImage load(File file, boolean linearScale) throws IOException;
}
