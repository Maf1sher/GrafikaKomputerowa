package org.mafisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DrawingPanel extends JPanel {
    private List<Shape> shapes;
    private String currentTool;
    private Shape selectedShape;
    private int selectedHandle;
    private Point lastMousePoint;
    private boolean dragging;
    private boolean resizing;

    private final List<ShapeChangeListener> listeners;

    public DrawingPanel() {
        shapes = new ArrayList<>();
        listeners = new ArrayList<>();
        currentTool = "SELECT";
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        });
    }

    public void addShapeChangeListener(ShapeChangeListener listener) {
        listeners.add(listener);
    }

    public void removeShapeChangeListener(ShapeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ShapeChangeListener listener : listeners) {
            listener.onShapeChanged(this.selectedShape);
        }
    }

    private void setSelectedShape(Shape shape) {
        if (shape == null){
            if (selectedShape != null)
                selectedShape.setSelected(false);
        }else {
            if (selectedShape != null)
                selectedShape.setSelected(false);
            shape.setSelected(true);
        }
        selectedShape = shape;
    }

    private void handleMousePressed(MouseEvent e) {
        Point startPoint = e.getPoint();
        lastMousePoint = e.getPoint();

        if (currentTool.equals("SELECT")) {
            if (selectedShape != null) {
                selectedHandle = selectedShape.getHandleAt(startPoint);
                if (selectedHandle != -1) {
                    resizing = true;
                    return;
                }
                setSelectedShape(null);
            }

            for (int i = shapes.size() - 1; i >= 0; i--) {
                Shape shape = shapes.get(i);
                if (shape.contains(startPoint)) {
                    setSelectedShape(shape);
                    dragging = true;
                    break;
                }
            }
            notifyListeners();
            repaint();
        }else{
            Shape shape = switch (currentTool) {
                case "LINE" -> new Line(startPoint, startPoint);
                case "RECTANGLE" -> new Rectangle(startPoint, 0, 0);
                case "CIRCLE" -> new Circle(startPoint, 0);
                default -> null;
            };
            setSelectedShape(shape);
            shapes.add(shape);
            currentTool = "SELECT";
            resizing = true;
            selectedHandle = selectedShape.getHandleAt(startPoint);
            repaint();
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        Point currentPoint = e.getPoint();

        if (currentTool.equals("SELECT")) {
            if (resizing && selectedShape != null) {
                selectedShape.resize(currentPoint, selectedHandle);
                repaint();
            } else if (dragging && selectedShape != null) {
                int dx = currentPoint.x - lastMousePoint.x;
                int dy = currentPoint.y - lastMousePoint.y;
                selectedShape.move(dx, dy);
                lastMousePoint = currentPoint;
                repaint();
            }
            notifyListeners();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
            dragging = false;
            resizing = false;
            repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Shape shape : shapes) {
            shape.draw(g2d);
        }
    }

    public void setCurrentTool(String tool) {
        this.currentTool = tool;
        if (!tool.equals("SELECT")) {
            setSelectedShape(null);
        }
        repaint();
    }

    public void addShapeFromParameters(String type, String params) {
        Shape shape = null;
        try {
            shape = switch (type) {
                case "LINE" -> new Line(new Point(0, 0), new Point(0, 0));
                case "RECTANGLE" -> new Rectangle(new Point(0, 0), 0, 0);
                case "CIRCLE" -> new Circle(new Point(0, 0), 0);
                default -> shape;
            };
            if (shape != null) {
                shape.setParameters(params);
                shapes.add(shape);
                setSelectedShape(shape);
                repaint();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błędne parametry!", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateSelectedShape(String params) {
        if (selectedShape != null) {
            try {
                selectedShape.setParameters(params);
                repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błędne parametry!", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getSelectedShapeParameters() {
        return selectedShape != null ? selectedShape.getParameters() : "";
    }

    public String getSelectedShapeType() {
        return switch (selectedShape) {
            case Line line -> "LINE";
            case Rectangle rectangle -> "RECTANGLE";
            case Circle circle -> "CIRCLE";
            case null, default -> null;
        };
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public void saveToFile(String filename) {
        selectedShape.setSelected(false);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(shapes);
            JOptionPane.showMessageDialog(this, "Zapisano pomyślnie do pliku: " + filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            shapes = (List<Shape>) ois.readObject();
            selectedShape = null;
            repaint();
            JOptionPane.showMessageDialog(this, "Wczytano pomyślnie z pliku: " + filename);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Błąd odczytu: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearAll() {
        shapes.clear();
        selectedShape = null;
        repaint();
    }
}