package org.mafisher;

import javax.swing.*;
import java.awt.*;

public class CMYKColorPanel extends JPanel implements ColorObserver {
    private ColorModel colorModel;
    private ColorSlider cyanSlider;
    private ColorSlider magentaSlider;
    private ColorSlider yellowSlider;
    private ColorSlider blackSlider;

    public CMYKColorPanel(ColorModel colorModel) {
        this.colorModel = colorModel;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("CMYK Color Space"));

        JPanel slidersPanel = new JPanel();
        slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.Y_AXIS));

        cyanSlider = new ColorSlider("Cyan", Color.CYAN, value -> {
            colorModel.setCMYK(value, colorModel.getM(), colorModel.getY(), colorModel.getK());
        });

        magentaSlider = new ColorSlider("Magenta", Color.MAGENTA, value -> {
            colorModel.setCMYK(colorModel.getC(), value, colorModel.getY(), colorModel.getK());
        });

        yellowSlider = new ColorSlider("Yellow", Color.YELLOW, value -> {
            colorModel.setCMYK(colorModel.getC(), colorModel.getM(), value, colorModel.getK());
        });

        blackSlider = new ColorSlider("Black", Color.BLACK, value -> {
            colorModel.setCMYK(colorModel.getC(), colorModel.getM(), colorModel.getY(), value);
        });

        slidersPanel.add(cyanSlider);
        slidersPanel.add(Box.createVerticalStrut(10));
        slidersPanel.add(magentaSlider);
        slidersPanel.add(Box.createVerticalStrut(10));
        slidersPanel.add(yellowSlider);
        slidersPanel.add(Box.createVerticalStrut(10));
        slidersPanel.add(blackSlider);

        add(slidersPanel, BorderLayout.CENTER);
    }

    @Override
    public void onColorChanged() {
        cyanSlider.setValue(colorModel.getC());
        magentaSlider.setValue(colorModel.getM());
        yellowSlider.setValue(colorModel.getY());
        blackSlider.setValue(colorModel.getK());
    }
}
