package org.mafisher;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class ControlPanel extends JPanel implements ShapeChangeListener {
    private final DrawingPanel drawingPanel;
    private final JPanel paramInputPanel;

    private JTextField lineX1, lineY1, lineX2, lineY2;
    private JTextField rectX, rectY, rectWidth, rectHeight;
    private JTextField circleX, circleY, circleRadius;
    private JTextField polyPoints;
    private JTextArea bezierPointsArea;

    private JTextField transDx, transDy;
    private JTextField rotAngle, rotPx, rotPy;
    private JTextField scaleFactor, scalePx, scalePy;

    private boolean isUpdatingFields = false;
    private String currentShapeType = "LINE";

    public ControlPanel(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = new Insets(2, 2, 2, 2);

        JPanel toolsPanel = new JPanel(new GridLayout(2, 3, 2, 2));
        toolsPanel.setBorder(new TitledBorder("Narzędzia"));

        JButton btnSel = new JButton("Wybierz");
        JButton btnLine = new JButton("Linia");
        JButton btnRect = new JButton("Prost.");
        JButton btnCirc = new JButton("Okrąg");
        JButton btnPoly = new JButton("Wielokąt");
        JButton btnBez = new JButton("Bézier");

        btnSel.addActionListener(e -> setTool("SELECT"));
        btnLine.addActionListener(e -> setTool("LINE"));
        btnRect.addActionListener(e -> setTool("RECTANGLE"));
        btnCirc.addActionListener(e -> setTool("CIRCLE"));
        btnPoly.addActionListener(e -> setTool("POLYGON"));
        btnBez.addActionListener(e -> setupBezier());

        toolsPanel.add(btnSel); toolsPanel.add(btnLine); toolsPanel.add(btnRect);
        toolsPanel.add(btnCirc); toolsPanel.add(btnPoly); toolsPanel.add(btnBez);

        gbc.gridy = 0; add(toolsPanel, gbc);

        JPanel modePanel = new JPanel(new GridLayout(1, 3));
        modePanel.setBorder(new TitledBorder("Mysz (Tryb)"));
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton rbMove = new JRadioButton("Przesuń", true);
        JRadioButton rbRot = new JRadioButton("Obróć");
        JRadioButton rbScale = new JRadioButton("Skaluj");

        modeGroup.add(rbMove); modeGroup.add(rbRot); modeGroup.add(rbScale);
        rbMove.addActionListener(e -> drawingPanel.setInteractionMode("MOVE"));
        rbRot.addActionListener(e -> drawingPanel.setInteractionMode("ROTATE"));
        rbScale.addActionListener(e -> drawingPanel.setInteractionMode("SCALE"));

        modePanel.add(rbMove); modePanel.add(rbRot); modePanel.add(rbScale);

        gbc.gridy = 1; add(modePanel, gbc);

        JPanel paramsContainer = new JPanel(new BorderLayout());
        paramsContainer.setBorder(new TitledBorder("Parametry"));
        paramInputPanel = new JPanel(new GridBagLayout());
        paramsContainer.add(paramInputPanel, BorderLayout.CENTER);

        JButton btnUpdate = new JButton("Aktualizuj");
        btnUpdate.addActionListener(e -> addShapeFromInputFields());
        paramsContainer.add(btnUpdate, BorderLayout.SOUTH);

        initializeParameterFields();
        updateParameterInputPanel("LINE");

        gbc.gridy = 2; add(paramsContainer, gbc);

        JPanel transPanel = new JPanel(new GridBagLayout());
        transPanel.setBorder(new TitledBorder("Transformacje"));
        GridBagConstraints tgbc = new GridBagConstraints();
        tgbc.fill = GridBagConstraints.HORIZONTAL; tgbc.weightx = 1.0; tgbc.insets = new Insets(1,1,1,1);

        transDx = new JTextField("0", 3); transDy = new JTextField("0", 3);
        JButton btnTrans = new JButton("OK");
        btnTrans.addActionListener(e -> applyTransform("TRANSLATE"));
        addTransformRow(transPanel, tgbc, 0, "Przesuń:", transDx, transDy, btnTrans);

        rotAngle = new JTextField("45", 3);
        rotPx = new JTextField("-1", 3); rotPy = new JTextField("-1", 3);
        JButton btnRot = new JButton("OK");
        btnRot.addActionListener(e -> applyTransform("ROTATE"));
        addPivotListener(rotPx, rotPy);
        addTransformRow(transPanel, tgbc, 1, "Obrót(deg):", rotAngle, null, null);
        addTransformRow(transPanel, tgbc, 2, "Pivot(x,y):", rotPx, rotPy, btnRot);

        scaleFactor = new JTextField("1.5", 3);
        scalePx = new JTextField("-1", 3); scalePy = new JTextField("-1", 3);
        JButton btnScale = new JButton("OK");
        btnScale.addActionListener(e -> applyTransform("SCALE"));
        addPivotListener(scalePx, scalePy);
        addTransformRow(transPanel, tgbc, 3, "Skala:", scaleFactor, null, null);
        addTransformRow(transPanel, tgbc, 4, "Pivot(x,y):", scalePx, scalePy, btnScale);

        gbc.gridy = 3; add(transPanel, gbc);

        JPanel filePanel = new JPanel(new GridLayout(1, 3, 2, 2));
        JButton btnSave = new JButton("Zapisz");
        JButton btnLoad = new JButton("Wczytaj");
        JButton btnClear = new JButton("Wyczyść");

        btnSave.addActionListener(e -> saveFile());
        btnLoad.addActionListener(e -> loadFile());
        btnClear.addActionListener(e -> confirmAndClear());

        filePanel.add(btnSave); filePanel.add(btnLoad); filePanel.add(btnClear);
        gbc.gridy = 4; gbc.weighty = 1.0; gbc.anchor = GridBagConstraints.NORTH;
        add(filePanel, gbc);
    }

    private void addTransformRow(JPanel p, GridBagConstraints gbc, int row, String lbl, JTextField f1, JTextField f2, JButton btn) {
        gbc.gridy = row;
        gbc.gridx = 0; p.add(new JLabel(lbl), gbc);
        gbc.gridx = 1; p.add(f1, gbc);
        if(f2 != null) { gbc.gridx = 2; p.add(f2, gbc); }
        if(btn != null) { gbc.gridx = 3; p.add(btn, gbc); }
    }

    private void addPivotListener(JTextField px, JTextField py) {
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            void update() {
                try {
                    int x = Integer.parseInt(px.getText().trim());
                    int y = Integer.parseInt(py.getText().trim());
                    if (x != -1 && y != -1) drawingPanel.setCustomPivot(new Point(x, y));
                } catch(Exception e) { /* ignore incomplete input */ }
            }
        };
        px.getDocument().addDocumentListener(dl);
        py.getDocument().addDocumentListener(dl);
    }

    private void setTool(String tool) {
        drawingPanel.setCurrentTool(tool);
        currentShapeType = tool;
        updateParameterInputPanel(tool);
    }

    private void setupBezier() {
        String res = JOptionPane.showInputDialog(this, "Stopień:", "2");
        if(res != null) {
            try {
                drawingPanel.setBezierDegree(Integer.parseInt(res));
                setTool("BEZIER");
            } catch(NumberFormatException e) {}
        }
    }

    private void initializeParameterFields() {
        lineX1 = new JTextField(3); lineY1 = new JTextField(3);
        lineX2 = new JTextField(3); lineY2 = new JTextField(3);
        rectX = new JTextField(3); rectY = new JTextField(3);
        rectWidth = new JTextField(3); rectHeight = new JTextField(3);
        circleX = new JTextField(3); circleY = new JTextField(3);
        circleRadius = new JTextField(3);
        polyPoints = new JTextField(10);
        bezierPointsArea = new JTextArea(3, 15);

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { pushUpdate(); }
            public void removeUpdate(DocumentEvent e) { pushUpdate(); }
            public void changedUpdate(DocumentEvent e) { pushUpdate(); }
            void pushUpdate() {
                if(drawingPanel.getSelectedShape() != null) updateSelectedShapeFromFields();
            }
        };

        lineX1.getDocument().addDocumentListener(dl); lineY1.getDocument().addDocumentListener(dl);
        lineX2.getDocument().addDocumentListener(dl); lineY2.getDocument().addDocumentListener(dl);
        rectX.getDocument().addDocumentListener(dl); rectY.getDocument().addDocumentListener(dl);
        rectWidth.getDocument().addDocumentListener(dl); rectHeight.getDocument().addDocumentListener(dl);
        circleX.getDocument().addDocumentListener(dl); circleY.getDocument().addDocumentListener(dl);
        circleRadius.getDocument().addDocumentListener(dl);
        polyPoints.getDocument().addDocumentListener(dl);
    }

    private void updateParameterInputPanel(String type) {
        paramInputPanel.removeAll();
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2,2,2,2); g.fill = GridBagConstraints.HORIZONTAL;

        switch (type) {
            case "LINE":
                addRow(0, "X1,Y1", lineX1, lineY1, g);
                addRow(1, "X2,Y2", lineX2, lineY2, g);
                break;
            case "RECTANGLE":
                addRow(0, "X,Y", rectX, rectY, g);
                addRow(1, "W,H", rectWidth, rectHeight, g);
                break;
            case "CIRCLE":
                addRow(0, "X,Y", circleX, circleY, g);
                addRow(1, "R", circleRadius, null, g);
                break;
            case "POLYGON":
                g.gridx=0; g.gridy=0; g.gridwidth=2;
                paramInputPanel.add(new JLabel("x1,y1;x2,y2..."), g);
                g.gridy=1; paramInputPanel.add(polyPoints, g);
                break;
            case "BEZIER":
                g.gridx=0; g.gridy=0; g.gridwidth=2;
                paramInputPanel.add(new JLabel("Punkty kontrolne:"), g);
                g.gridy=1; paramInputPanel.add(new JScrollPane(bezierPointsArea), g);
                break;
        }
        paramInputPanel.revalidate(); paramInputPanel.repaint();
    }

    private void addRow(int row, String lbl, JTextField f1, JTextField f2, GridBagConstraints g) {
        g.gridy = row; g.gridwidth = 1;
        g.gridx = 0; paramInputPanel.add(new JLabel(lbl), g);
        g.gridx = 1; paramInputPanel.add(f1, g);
        if(f2 != null) { g.gridx = 2; paramInputPanel.add(f2, g); }
    }

    private void updateSelectedShapeFromFields() {
        if (isUpdatingFields) return;
        try {
            String p = "";
            switch (currentShapeType) {
                case "LINE": p = String.format("%s,%s,%s,%s", lineX1.getText(), lineY1.getText(), lineX2.getText(), lineY2.getText()); break;
                case "RECTANGLE": p = String.format("%s,%s,%s,%s", rectX.getText(), rectY.getText(), rectWidth.getText(), rectHeight.getText()); break;
                case "CIRCLE": p = String.format("%s,%s,%s", circleX.getText(), circleY.getText(), circleRadius.getText()); break;
                case "POLYGON": p = polyPoints.getText(); break;
                case "BEZIER": p = bezierPointsArea.getText(); break;
            }
            drawingPanel.updateSelectedShape(p);
        } catch (Exception e) {}
    }

    private void addShapeFromInputFields() {
        if (drawingPanel.getSelectedShape() == null) {
            try {
                String p = "";
                switch (currentShapeType) {
                    case "LINE": p = String.format("%s,%s,%s,%s", lineX1.getText(), lineY1.getText(), lineX2.getText(), lineY2.getText()); break;
                    case "RECTANGLE": p = String.format("%s,%s,%s,%s", rectX.getText(), rectY.getText(), rectWidth.getText(), rectHeight.getText()); break;
                    case "CIRCLE": p = String.format("%s,%s,%s", circleX.getText(), circleY.getText(), circleRadius.getText()); break;
                    case "POLYGON": p = polyPoints.getText(); break;
                    case "BEZIER": p = bezierPointsArea.getText(); break;
                }
                drawingPanel.addShapeFromParameters(currentShapeType, p);
            } catch(Exception e){}
        }
    }

    private void applyTransform(String type) {
        try {
            double v1=0, v2=-1, v3=-1;
            if(type.equals("TRANSLATE")) { v1 = d(transDx); v2 = d(transDy); }
            else if(type.equals("ROTATE")) { v1 = d(rotAngle); v2 = d(rotPx); v3 = d(rotPy); }
            else if(type.equals("SCALE")) { v1 = d(scaleFactor); v2 = d(scalePx); v3 = d(scalePy); }
            drawingPanel.applyTransformation(type, v1, v2, v3);
        } catch(Exception e) { JOptionPane.showMessageDialog(this, "Błąd liczbowy"); }
    }

    private double d(JTextField f) { return Double.parseDouble(f.getText()); }

    @Override
    public void onShapeChanged(Shape s) {
        if(s == null) return;
        isUpdatingFields = true;
        currentShapeType = drawingPanel.getSelectedShapeType();
        updateParameterInputPanel(currentShapeType);

        Point pivot = drawingPanel.getCurrentPivot();
        if (pivot != null) {
            String px = String.valueOf(pivot.x);
            String py = String.valueOf(pivot.y);
            rotPx.setText(px); rotPy.setText(py);
            scalePx.setText(px); scalePy.setText(py);
        }

        String params = s.getParameters();
        if(currentShapeType.equals("POLYGON")) polyPoints.setText(params);
        else if(currentShapeType.equals("BEZIER")) bezierPointsArea.setText(params);
        else if(currentShapeType.equals("RECTANGLE")) {
            if(!params.contains(";")) {
                String[] p = params.split(",");
                rectX.setText(p[0]); rectY.setText(p[1]); rectWidth.setText(p[2]); rectHeight.setText(p[3]);
            }
        } else if(currentShapeType.equals("LINE")) {
            String[] p = params.split(",");
            lineX1.setText(p[0]); lineY1.setText(p[1]); lineX2.setText(p[2]); lineY2.setText(p[3]);
        } else if(currentShapeType.equals("CIRCLE")) {
            String[] p = params.split(",");
            circleX.setText(p[0]); circleY.setText(p[1]); circleRadius.setText(p[2]);
        }
        isUpdatingFields = false;
    }

    private void saveFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            drawingPanel.saveToFile(fc.getSelectedFile().getAbsolutePath());
    }

    private void loadFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            drawingPanel.loadFromFile(fc.getSelectedFile().getAbsolutePath());
    }

    private void confirmAndClear() {
        if (JOptionPane.showConfirmDialog(this, "Wyczyścić?") == JOptionPane.YES_OPTION)
            drawingPanel.clearAll();
    }
}