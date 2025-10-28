package org.mafisher;

import javax.swing.*;
import java.awt.*;

public class ColorPreviewPanel extends JPanel implements ColorObserver {
    private ColorModel colorModel;
    private JPanel colorDisplay;

    public ColorPreviewPanel(ColorModel colorModel) {
        this.colorModel = colorModel;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Color Preview"));
        setPreferredSize(new Dimension(0, 100));

        colorDisplay = new JPanel();
        colorDisplay.setPreferredSize(new Dimension(0, 60));
        add(colorDisplay, BorderLayout.CENTER);

        updateColor();
    }

    @Override
    public void onColorChanged() {
        updateColor();
    }

    private void updateColor() {
        int r = (int)(colorModel.getR() * 255);
        int g = (int)(colorModel.getG() * 255);
        int b = (int)(colorModel.getB() * 255);
        colorDisplay.setBackground(new Color(r, g, b));
    }
}
