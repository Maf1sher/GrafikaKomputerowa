package org.mafisher;

import javax.swing.*;
import java.awt.*;

public class RGBCubePanel extends JPanel {
    private CubeRenderer cubeRenderer;
    private JPanel controlPanel;
    private JSlider crossSectionSlider;
    private JComboBox<String> axisComboBox;

    public RGBCubePanel() {
        setLayout(new BorderLayout());

        cubeRenderer = new CubeRenderer();
        add(cubeRenderer, BorderLayout.CENTER);

        controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Cross Section Control"));

        panel.add(new JLabel("Axis:"));
        axisComboBox = new JComboBox<>(new String[]{"X (Red)", "Y (Green)", "Z (Blue)"});
        axisComboBox.addActionListener(e -> {
            cubeRenderer.setCrossSectionAxis(axisComboBox.getSelectedIndex());
        });
        panel.add(axisComboBox);

        panel.add(new JLabel("Position:"));
        crossSectionSlider = new JSlider(0, 100, 50);
        crossSectionSlider.addChangeListener(e -> {
            cubeRenderer.setCrossSectionPosition(crossSectionSlider.getValue() / 100.0f);
        });
        panel.add(crossSectionSlider);

        JCheckBox showCrossSectionCheckBox = new JCheckBox("Show Cross Section", false);
        showCrossSectionCheckBox.addActionListener(e -> {
            cubeRenderer.setShowCrossSection(showCrossSectionCheckBox.isSelected());
        });
        panel.add(showCrossSectionCheckBox);

        return panel;
    }
}
