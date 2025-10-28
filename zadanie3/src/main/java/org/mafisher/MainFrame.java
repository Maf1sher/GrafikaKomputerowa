package org.mafisher;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Color Space Converter & RGB Cube");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Color Converter", new ColorConverterPanel());
        tabbedPane.addTab("RGB Cube", new RGBCubePanel());

        add(tabbedPane);
    }
}
