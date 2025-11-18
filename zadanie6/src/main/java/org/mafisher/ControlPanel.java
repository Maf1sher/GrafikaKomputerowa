package org.mafisher;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ControlPanel extends JPanel implements ShapeChangeListener {
    private final DrawingPanel drawingPanel;
    private final JPanel paramInputPanel;

    private JTextField lineX1, lineY1, lineX2, lineY2;
    private JTextField rectX, rectY, rectWidth, rectHeight;
    private JTextField circleX, circleY, circleRadius;
    private JTextField bezierDegreeField;
    private List<JTextField> bezierPointFields;

    private boolean isUpdatingFields = false;
    private String currentShapeType = "LINE";

    public ControlPanel(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
        bezierPointFields = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectBtn = new JButton("Wybierz");
        JButton lineBtn = new JButton("Linia");
        JButton rectBtn = new JButton("Prostokąt");
        JButton circleBtn = new JButton("Okrąg");
        JButton bezierBtn = new JButton("Krzywa Béziera");

        selectBtn.addActionListener(e -> {
            drawingPanel.setCurrentTool("SELECT");
        });

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

        bezierBtn.addActionListener(e -> {
            String degreeStr = JOptionPane.showInputDialog(this,
                    "Podaj stopień krzywej Béziera:", "2");
            if (degreeStr != null) {
                try {
                    int degree = Integer.parseInt(degreeStr);
                    if (degree >= 1 && degree <= 10) {
                        drawingPanel.setBezierDegree(degree);
                        drawingPanel.setCurrentTool("BEZIER");
                        currentShapeType = "BEZIER";
                        updateParameterInputPanel("BEZIER");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Stopień musi być między 1 a 10",
                                "Błąd", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Nieprawidłowy format liczby",
                            "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        toolsPanel.add(selectBtn);
        toolsPanel.add(lineBtn);
        toolsPanel.add(rectBtn);
        toolsPanel.add(circleBtn);
        toolsPanel.add(bezierBtn);

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

        bezierDegreeField = new JTextField(4);

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
                if (drawingPanel.getSelectedShape() != null)
                    updateSelectedShapeFromFields();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (drawingPanel.getSelectedShape() != null)
                    updateSelectedShapeFromFields();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (drawingPanel.getSelectedShape() != null)
                    updateSelectedShapeFromFields();
            }
        });
    }

    private void updateSelectedShapeFromFields() {
        if (isUpdatingFields) return;

        String type = drawingPanel.getSelectedShapeType();
        if (type == null) return;

        try {
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
                case "BEZIER":
                    if (drawingPanel.getSelectedShape() instanceof BezierCurve) {
                        BezierCurve bezier = (BezierCurve) drawingPanel.getSelectedShape();
                        StringBuilder sb = new StringBuilder();
                        sb.append(bezier.getDegree()).append(";");

                        boolean allFilled = true;
                        for (int i = 0; i < bezierPointFields.size(); i += 2) {
                            if (i + 1 < bezierPointFields.size()) {
                                String xStr = bezierPointFields.get(i).getText();
                                String yStr = bezierPointFields.get(i + 1).getText();
                                if (xStr.isEmpty() || yStr.isEmpty()) {
                                    allFilled = false;
                                    break;
                                }
                                sb.append(xStr).append(",").append(yStr);
                                if (i + 2 < bezierPointFields.size()) {
                                    sb.append(";");
                                }
                            }
                        }

                        if (allFilled) {
                            drawingPanel.updateSelectedShape(sb.toString());
                        }
                    }
                    break;
            }
        } catch (Exception ex) {
            // ignore
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

            case "BEZIER":
                Shape selected = drawingPanel.getSelectedShape();
                int pointCount = 3; // domyślnie

                if (selected instanceof BezierCurve) {
                    BezierCurve bezier = (BezierCurve) selected;
                    pointCount = bezier.getDegree() + 1;
                }

                paramInputPanel.add(new JLabel("Stopień: " + (pointCount - 1) + " | "));

                bezierPointFields.clear();
                for (int i = 0; i < pointCount; i++) {
                    JTextField xField = new JTextField(4);
                    JTextField yField = new JTextField(4);
                    addAutoUpdateListener(xField);
                    addAutoUpdateListener(yField);
                    bezierPointFields.add(xField);
                    bezierPointFields.add(yField);

                    paramInputPanel.add(new JLabel("P" + i + "(x:"));
                    paramInputPanel.add(xField);
                    paramInputPanel.add(new JLabel("y:"));
                    paramInputPanel.add(yField);
                    paramInputPanel.add(new JLabel(") "));
                }
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
                case "BEZIER" -> {
                    StringBuilder sb = new StringBuilder();
                    int degree = (bezierPointFields.size() / 2) - 1;
                    sb.append(degree).append(";");

                    for (int i = 0; i < bezierPointFields.size(); i += 2) {
                        if (i + 1 < bezierPointFields.size()) {
                            sb.append(bezierPointFields.get(i).getText())
                                    .append(",")
                                    .append(bezierPointFields.get(i + 1).getText());
                            if (i + 2 < bezierPointFields.size()) {
                                sb.append(";");
                            }
                        }
                    }
                    yield sb.toString();
                }
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

            String[] values = params.split(";");
            switch (type) {
                case "LINE":
                    String[] lineValues = values[0].split(",");
                    if (lineValues.length == 4) {
                        if (!lineX1.isFocusOwner()) lineX1.setText(lineValues[0].trim());
                        if (!lineY1.isFocusOwner()) lineY1.setText(lineValues[1].trim());
                        if (!lineX2.isFocusOwner()) lineX2.setText(lineValues[2].trim());
                        if (!lineY2.isFocusOwner()) lineY2.setText(lineValues[3].trim());
                    }
                    break;
                case "RECTANGLE":
                    String[] rectValues = values[0].split(",");
                    if (rectValues.length == 4) {
                        if (!rectX.isFocusOwner()) rectX.setText(rectValues[0].trim());
                        if (!rectY.isFocusOwner()) rectY.setText(rectValues[1].trim());
                        if (!rectWidth.isFocusOwner()) rectWidth.setText(rectValues[2].trim());
                        if (!rectHeight.isFocusOwner()) rectHeight.setText(rectValues[3].trim());
                    }
                    break;
                case "CIRCLE":
                    String[] circleValues = values[0].split(",");
                    if (circleValues.length == 3) {
                        if (!circleX.isFocusOwner()) circleX.setText(circleValues[0].trim());
                        if (!circleY.isFocusOwner()) circleY.setText(circleValues[1].trim());
                        if (!circleRadius.isFocusOwner()) circleRadius.setText(circleValues[2].trim());
                    }
                    break;
                case "BEZIER":
                    if (values.length >= 2) {
                        for (int i = 1; i < values.length; i++) {
                            String[] coords = values[i].split(",");
                            if (coords.length == 2) {
                                int fieldIndex = (i - 1) * 2;
                                if (fieldIndex + 1 < bezierPointFields.size()) {
                                    JTextField xField = bezierPointFields.get(fieldIndex);
                                    JTextField yField = bezierPointFields.get(fieldIndex + 1);
                                    if (!xField.isFocusOwner()) xField.setText(coords[0].trim());
                                    if (!yField.isFocusOwner()) yField.setText(coords[1].trim());
                                }
                            }
                        }
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
            case "BEZIER":
                for (JTextField field : bezierPointFields) {
                    field.setText("");
                }
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