package org.mafisher;

import javax.swing.*;
import java.awt.*;

public class ColorConverterPanel extends JPanel {
    private RGBColorPanel rgbPanel;
    private CMYKColorPanel cmykPanel;
    private ColorPreviewPanel previewPanel;
    private ColorModel colorModel;

    public ColorConverterPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        colorModel = new ColorModel();

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        rgbPanel = new RGBColorPanel(colorModel);
        cmykPanel = new CMYKColorPanel(colorModel);

        topPanel.add(rgbPanel);
        topPanel.add(cmykPanel);

        previewPanel = new ColorPreviewPanel(colorModel);

        add(topPanel, BorderLayout.CENTER);
        add(previewPanel, BorderLayout.SOUTH);

        colorModel.addObserver(rgbPanel);
        colorModel.addObserver(cmykPanel);
        colorModel.addObserver(previewPanel);
    }
}
