package org.mafisher;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;

public class ColorSlider extends JPanel {
    private JSlider slider;
    private JTextField textField;
    private JLabel label;
    private ColorChangeListener listener;
    private boolean updating = false;
    private DecimalFormat df = new DecimalFormat("0.00");

    public ColorSlider(String name, Color color, ColorChangeListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout(5, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        label = new JLabel(name + ":");
        label.setPreferredSize(new Dimension(70, 25));
        add(label, BorderLayout.WEST);

        slider = new JSlider(0, 100, 50);
        slider.addChangeListener(e -> {
            if (!updating) {
                updating = true;
                float value = slider.getValue() / 100.0f;
                textField.setText(df.format(value));
                listener.onColorChange(value);
                updating = false;
            }
        });
        add(slider, BorderLayout.CENTER);

        textField = new JTextField(5);
        textField.setText("0.50");
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }

            private void update() {
                if (!updating) {
                    try {
                        updating = true;
                        float value = Float.parseFloat(textField.getText());
                        value = Math.max(0, Math.min(1, value));
                        slider.setValue((int)(value * 100));
                        listener.onColorChange(value);
                        updating = false;
                    } catch (NumberFormatException ex) {
                        updating = false;
                    }
                }
            }
        });
        add(textField, BorderLayout.EAST);
    }

    public void setValue(float value) {
        if (!updating) {
            updating = true;
            slider.setValue((int)(value * 100));
            textField.setText(df.format(value));
            updating = false;
        }
    }

    public interface ColorChangeListener {
        void onColorChange(float value);
    }
}
