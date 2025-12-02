package org.mafisher;

import javax.swing.*;
import java.awt.*;

public class SEEditorDialog extends JDialog {
    private final int size;
    private final JCheckBox[][] checkBoxes;
    private final JComboBox<String> filterTypeCombo;
    private boolean confirmed = false;

    public SEEditorDialog(Frame owner, int size) {
        super(owner, "Edytor elementu strukturyzującego", true);
        this.size = size;
        this.checkBoxes = new JCheckBox[size][size];

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Typ filtra:"));
        filterTypeCombo = new JComboBox<>(new String[]{"Dylatacja", "Erozja", "Otwarcie", "Domknięcie"});
        topPanel.add(filterTypeCombo);
        add(topPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(size, size, 2, 2));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                checkBoxes[i][j] = new JCheckBox();
                checkBoxes[i][j].setHorizontalAlignment(SwingConstants.CENTER);

                if (i == size/2 && j == size/2) {
                    checkBoxes[i][j].setSelected(true);
                    checkBoxes[i][j].setBackground(Color.LIGHT_GRAY);
                }

                gridPanel.add(checkBoxes[i][j]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton presetCrossBtn = new JButton("Krzyż");
        presetCrossBtn.addActionListener(e -> applyPresetCross());
        buttonPanel.add(presetCrossBtn);

        JButton presetSquareBtn = new JButton("Kwadrat");
        presetSquareBtn.addActionListener(e -> applyPresetSquare());
        buttonPanel.add(presetSquareBtn);

        JButton clearBtn = new JButton("Wyczyść");
        clearBtn.addActionListener(e -> clearAll());
        buttonPanel.add(clearBtn);

        buttonPanel.add(Box.createHorizontalStrut(20));

        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        buttonPanel.add(okBtn);

        JButton cancelBtn = new JButton("Anuluj");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void applyPresetCross() {
        clearAll();
        int center = size / 2;
        for (int i = 0; i < size; i++) {
            checkBoxes[center][i].setSelected(true);
            checkBoxes[i][center].setSelected(true);
        }
    }

    private void applyPresetSquare() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                checkBoxes[i][j].setSelected(true);
            }
        }
    }

    private void clearAll() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                checkBoxes[i][j].setSelected(false);
            }
        }
        int center = size / 2;
        checkBoxes[center][center].setSelected(true);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int[][] getStructuringElement() {
        int[][] se = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                se[i][j] = checkBoxes[i][j].isSelected() ? 1 : 0;
            }
        }
        return se;
    }

    public String getFilterType() {
        return (String) filterTypeCombo.getSelectedItem();
    }
}