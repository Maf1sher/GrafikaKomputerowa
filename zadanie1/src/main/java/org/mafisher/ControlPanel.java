package org.mafisher;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

public class ControlPanel extends JPanel implements ShapeChangeListener {
    private final DrawingPanel drawingPanel;
    private final JPanel paramInputPanel;

    private JTextField lineX1, lineY1, lineX2, lineY2;
    private JTextField rectX, rectY, rectWidth, rectHeight;
    private JTextField circleX, circleY, circleRadius;

    private boolean isUpdatingFields = false;
    private String currentShapeType = "LINE";

    public ControlPanel(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectBtn = new JButton("Wybierz");
        JButton lineBtn = new JButton("Linia");
        JButton rectBtn = new JButton("Prostokąt");
        JButton circleBtn = new JButton("Okrąg");

        selectBtn.addActionListener(e -> drawingPanel.setCurrentTool("SELECT"));
        lineBtn.addActionListener(e -> {
            drawingPanel.setCurrentTool("LINE");
            currentShapeType = "LINE";
            updateParameterInputPanel("LINE");
        });
        rectBtn.addActionListener(e -> {
            drawingPanel.setCurrentTool("RECTANGLE");
            currentShapeType = "RECTANGLE";
            updateParameterInputPanel("RECTANGLE");
        });
        circleBtn.addActionListener(e -> {
            drawingPanel.setCurrentTool("CIRCLE");
            currentShapeType = "CIRCLE";
            updateParameterInputPanel("CIRCLE");
        });

        toolsPanel.add(selectBtn);
        toolsPanel.add(lineBtn);
        toolsPanel.add(rectBtn);
        toolsPanel.add(circleBtn);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn = new JButton("Dodaj");
        addBtn.addActionListener(e -> addShapeFromInputFields());

        paramInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        initializeParameterFields();
        updateParameterInputPanel("LINE");

        addPanel.add(paramInputPanel);
        addPanel.add(addBtn);

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveBtn = new JButton("💾 Zapisz do pliku");
        JButton loadBtn = new JButton("📂 Wczytaj z pliku");
        JButton clearBtn = new JButton("🗑️ Wyczyść wszystko");

        saveBtn.setBackground(new Color(76, 175, 80));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);

        loadBtn.setBackground(new Color(33, 150, 243));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setFocusPainted(false);

        clearBtn.setBackground(new Color(244, 67, 54));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFocusPainted(false);

        saveBtn.addActionListener(e -> saveFile());
        loadBtn.addActionListener(e -> loadFile());
        clearBtn.addActionListener(e -> confirmAndClear());

        filePanel.add(saveBtn);
        filePanel.add(loadBtn);
        filePanel.add(clearBtn);

        add(toolsPanel);
        add(addPanel);
        add(filePanel);
    }

    private void initializeParameterFields() {
        lineX1 = new JTextField(4);
        lineY1 = new JTextField(4);
        lineX2 = new JTextField(4);
        lineY2 = new JTextField(4);

        rectX = new JTextField(4);
        rectY = new JTextField(4);
        rectWidth = new JTextField(4);
        rectHeight = new JTextField(4);

        circleX = new JTextField(4);
        circleY = new JTextField(4);
        circleRadius = new JTextField(4);

        addAutoUpdateListener(lineX1);
        addAutoUpdateListener(lineY1);
        addAutoUpdateListener(lineX2);
        addAutoUpdateListener(lineY2);

        addAutoUpdateListener(rectX);
        addAutoUpdateListener(rectY);
        addAutoUpdateListener(rectWidth);
        addAutoUpdateListener(rectHeight);

        addAutoUpdateListener(circleX);
        addAutoUpdateListener(circleY);
        addAutoUpdateListener(circleRadius);
    }

    private void addAutoUpdateListener(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(drawingPanel.getSelectedShape() != null)
                    updateSelectedShapeFromFields();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if(drawingPanel.getSelectedShape() != null)
                    updateSelectedShapeFromFields();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if(drawingPanel.getSelectedShape() != null)
                    updateSelectedShapeFromFields();;
            }
        });
    }

    private void updateSelectedShapeFromFields() {
        if (isUpdatingFields) return;

        String type = drawingPanel.getSelectedShapeType();
        if (type == null) return;
        String params = "";
        switch (type) {
            case "LINE":
                if (!lineX1.getText().isEmpty() && !lineY1.getText().isEmpty() &&
                        !lineX2.getText().isEmpty() && !lineY2.getText().isEmpty()) {
                    params = String.format("%s,%s,%s,%s",
                            lineX1.getText(), lineY1.getText(),
                            lineX2.getText(), lineY2.getText());
                    drawingPanel.updateSelectedShape(params);
                }
                break;
            case "RECTANGLE":
                if (!rectX.getText().isEmpty() && !rectY.getText().isEmpty() &&
                        !rectWidth.getText().isEmpty() && !rectHeight.getText().isEmpty()) {
                    params = String.format("%s,%s,%s,%s",
                            rectX.getText(), rectY.getText(),
                            rectWidth.getText(), rectHeight.getText());
                    drawingPanel.updateSelectedShape(params);
                }
                break;
            case "CIRCLE":
                if (!circleX.getText().isEmpty() && !circleY.getText().isEmpty() &&
                        !circleRadius.getText().isEmpty()) {
                    params = String.format("%s,%s,%s",
                            circleX.getText(), circleY.getText(),
                            circleRadius.getText());
                    drawingPanel.updateSelectedShape(params);
                }
                break;
        }
    }

    private void updateParameterInputPanel(String shapeType) {
        paramInputPanel.removeAll();

        switch (shapeType) {
            case "LINE":
                paramInputPanel.add(new JLabel("P1(x:"));
                paramInputPanel.add(lineX1);
                paramInputPanel.add(new JLabel("y:"));
                paramInputPanel.add(lineY1);
                paramInputPanel.add(new JLabel(") P2(x:"));
                paramInputPanel.add(lineX2);
                paramInputPanel.add(new JLabel("y:"));
                paramInputPanel.add(lineY2);
                paramInputPanel.add(new JLabel(")"));
                break;

            case "RECTANGLE":
                paramInputPanel.add(new JLabel("Punkt(x:"));
                paramInputPanel.add(rectX);
                paramInputPanel.add(new JLabel("y:"));
                paramInputPanel.add(rectY);
                paramInputPanel.add(new JLabel(") W:"));
                paramInputPanel.add(rectWidth);
                paramInputPanel.add(new JLabel("H:"));
                paramInputPanel.add(rectHeight);
                break;

            case "CIRCLE":
                paramInputPanel.add(new JLabel("Środek(x:"));
                paramInputPanel.add(circleX);
                paramInputPanel.add(new JLabel("y:"));
                paramInputPanel.add(circleY);
                paramInputPanel.add(new JLabel(") R:"));
                paramInputPanel.add(circleRadius);
                break;
        }

        paramInputPanel.revalidate();
        paramInputPanel.repaint();
    }

    private void addShapeFromInputFields() {
        String params = "";

        try {
            params = switch (currentShapeType) {
                case "LINE" -> String.format("%s,%s,%s,%s",
                        lineX1.getText(), lineY1.getText(),
                        lineX2.getText(), lineY2.getText());
                case "RECTANGLE" -> String.format("%s,%s,%s,%s",
                        rectX.getText(), rectY.getText(),
                        rectWidth.getText(), rectHeight.getText());
                case "CIRCLE" -> String.format("%s,%s,%s",
                        circleX.getText(), circleY.getText(),
                        circleRadius.getText());
                default -> params;
            };

            drawingPanel.addShapeFromParameters(currentShapeType, params);
            drawingPanel.setCurrentTool("SELECT");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Błędne parametry! Wprowadź liczby całkowite.",
                    "Błąd", JOptionPane.ERROR_MESSAGE);
            clearInputFields(currentShapeType);
        }
    }

    private void updateParameterDisplay() {
        String type = drawingPanel.getSelectedShapeType();
        String params = drawingPanel.getSelectedShapeParameters();


        if (type != null && !params.isEmpty()) {
            isUpdatingFields = true;

            currentShapeType = type;
            updateParameterInputPanel(type);

            String[] values = params.split(",");
            switch (type) {
                case "LINE":
                    if (values.length == 4) {
                        if (!lineX1.isFocusOwner()) lineX1.setText(values[0].trim());
                        if (!lineY1.isFocusOwner()) lineY1.setText(values[1].trim());
                        if (!lineX2.isFocusOwner()) lineX2.setText(values[2].trim());
                        if (!lineY2.isFocusOwner()) lineY2.setText(values[3].trim());
                    }
                    break;
                case "RECTANGLE":
                    if (values.length == 4) {
                        if (!rectX.isFocusOwner()) rectX.setText(values[0].trim());
                        if (!rectY.isFocusOwner()) rectY.setText(values[1].trim());
                        if (!rectWidth.isFocusOwner()) rectWidth.setText(values[2].trim());
                        if (!rectHeight.isFocusOwner()) rectHeight.setText(values[3].trim());
                    }
                    break;
                case "CIRCLE":
                    if (values.length == 3) {
                        if (!circleX.isFocusOwner()) circleX.setText(values[0].trim());
                        if (!circleY.isFocusOwner()) circleY.setText(values[1].trim());
                        if (!circleRadius.isFocusOwner()) circleRadius.setText(values[2].trim());
                    }
                    break;
            }
            isUpdatingFields = false;
        }
    }

    private void clearInputFields(String type) {
        switch (type) {
            case "LINE":
                lineX1.setText("");
                lineY1.setText("");
                lineX2.setText("");
                lineY2.setText("");
                break;
            case "RECTANGLE":
                rectX.setText("");
                rectY.setText("");
                rectWidth.setText("");
                rectHeight.setText("");
                break;
            case "CIRCLE":
                circleX.setText("");
                circleY.setText("");
                circleRadius.setText("");
                break;
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Zapisz rysunek");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Pliki rysunków (*.draw)", "draw"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filename = file.getAbsolutePath();
            if (!filename.toLowerCase().endsWith(".draw")) {
                filename += ".draw";
            }
            drawingPanel.saveToFile(filename);
        }
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wczytaj rysunek");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Pliki rysunków (*.draw)", "draw"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            drawingPanel.loadFromFile(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void confirmAndClear() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Czy na pewno chcesz usunąć wszystkie figury?\nTej operacji nie można cofnąć!",
                "Potwierdzenie czyszczenia",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            drawingPanel.clearAll();
        }
    }

    @Override
    public void onShapeChanged(Shape newShape) {
        updateParameterDisplay();
    }
}