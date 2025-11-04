package org.mafisher;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewerFrame frame = new ViewerFrame();
            frame.setVisible(true);
        });
    }
}