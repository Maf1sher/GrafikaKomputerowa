package org.mafisher;

import javax.swing.*;
import java.awt.*;

public class GraphicsEditor extends JFrame {

    public GraphicsEditor() {
        setTitle("Edytor Graficzny - Homogeniczny");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        DrawingPanel drawingPanel = new DrawingPanel();
        ControlPanel controlPanel = new ControlPanel(drawingPanel);

        drawingPanel.addShapeChangeListener(controlPanel);

        add(controlPanel, BorderLayout.EAST);
        add(drawingPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}