package org.mafisher;

import javax.swing.*;
import java.awt.*;

public class RGBColorPanel extends JPanel implements ColorObserver {
    private ColorModel colorModel;
    private ColorSlider redSlider;
    private ColorSlider greenSlider;
    private ColorSlider blueSlider;

    public RGBColorPanel(ColorModel colorModel) {
        this.colorModel = colorModel;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("RGB Color Space"));

        JPanel slidersPanel = new JPanel();
        slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.Y_AXIS));

        redSlider = new ColorSlider("Red", Color.RED, value -> {
            colorModel.setRGB(value, colorModel.getG(), colorModel.getB());
        });

        greenSlider = new ColorSlider("Green", Color.GREEN, value -> {
            colorModel.setRGB(colorModel.getR(), value, colorModel.getB());
        });

        blueSlider = new ColorSlider("Blue", Color.BLUE, value -> {
            colorModel.setRGB(colorModel.getR(), colorModel.getG(), value);
        });

        slidersPanel.add(redSlider);
        slidersPanel.add(Box.createVerticalStrut(10));
        slidersPanel.add(greenSlider);
        slidersPanel.add(Box.createVerticalStrut(10));
        slidersPanel.add(blueSlider);

        add(slidersPanel, BorderLayout.CENTER);
    }

    @Override
    public void onColorChanged() {
        redSlider.setValue(colorModel.getR());
        greenSlider.setValue(colorModel.getG());
        blueSlider.setValue(colorModel.getB());
    }
}
