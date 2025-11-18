package org.mafisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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
    private BufferedImage canvas;
    private Graphics2D canvasGraphics;

    private BezierCurve tempBezierCurve;
    private int bezierDegree = 2;

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

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                initializeCanvas();
            }
        });
    }

    private void initializeCanvas() {
        int width = Math.max(getWidth(), 1);
        int height = Math.max(getHeight(), 1);
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        canvasGraphics = canvas.createGraphics();
        canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        redrawCanvas();
    }

    private void redrawCanvas() {
        if (canvas == null || canvasGraphics == null) {
            return;
        }

        canvasGraphics.setColor(Color.WHITE);
        canvasGraphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Shape shape : shapes) {
            shape.draw(canvasGraphics);
        }

        if (tempBezierCurve != null) {
            tempBezierCurve.draw(canvasGraphics);
        }

        repaint();
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
        if (shape == null) {
            if (selectedShape != null)
                selectedShape.setSelected(false);
        } else {
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
            redrawCanvas();
        } else if (currentTool.equals("BEZIER")) {
            if (tempBezierCurve == null) {
                tempBezierCurve = new BezierCurve(bezierDegree);
            }

            tempBezierCurve.addControlPoint(startPoint);

            if (tempBezierCurve.isComplete()) {
                shapes.add(tempBezierCurve);
                setSelectedShape(tempBezierCurve);
                tempBezierCurve = null;
                currentTool = "SELECT";
                notifyListeners();
            }

            redrawCanvas();
        } else {
            Shape shape = switch (currentTool) {
                case "LINE" -> new Line(startPoint, startPoint);
                case "RECTANGLE" -> new Rectangle(startPoint, 0, 0);
                case "CIRCLE" -> new Circle(startPoint, 0);
                default -> null;
            };

            if (shape != null) {
                setSelectedShape(shape);
                shapes.add(shape);
                currentTool = "SELECT";
                resizing = true;
                selectedHandle = selectedShape.getHandleAt(startPoint);
                redrawCanvas();
            }
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        Point currentPoint = e.getPoint();

        if (currentTool.equals("SELECT")) {
            if (resizing && selectedShape != null) {
                selectedShape.resize(currentPoint, selectedHandle);
                redrawCanvas();
            } else if (dragging && selectedShape != null) {
                int dx = currentPoint.x - lastMousePoint.x;
                int dy = currentPoint.y - lastMousePoint.y;
                selectedShape.move(dx, dy);
                lastMousePoint = currentPoint;
                redrawCanvas();
            }
            notifyListeners();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        dragging = false;
        resizing = false;
        redrawCanvas();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (canvas == null) {
            initializeCanvas();
        }

        g.drawImage(canvas, 0, 0, null);
    }

    public void setCurrentTool(String tool) {
        this.currentTool = tool;
        if (!tool.equals("SELECT")) {
            setSelectedShape(null);
            tempBezierCurve = null;
        }
        redrawCanvas();
    }

    public void setBezierDegree(int degree) {
        this.bezierDegree = degree;
        tempBezierCurve = null;
    }

    public void addShapeFromParameters(String type, String params) {
        Shape shape = null;
        try {
            shape = switch (type) {
                case "LINE" -> new Line(new Point(0, 0), new Point(0, 0));
                case "RECTANGLE" -> new Rectangle(new Point(0, 0), 0, 0);
                case "CIRCLE" -> new Circle(new Point(0, 0), 0);
                case "BEZIER" -> {
                    String[] parts = params.split(";");
                    int degree = Integer.parseInt(parts[0].trim());
                    yield new BezierCurve(degree);
                }
                default -> null;
            };

            if (shape != null) {
                shape.setParameters(params);
                shapes.add(shape);
                setSelectedShape(shape);
                notifyListeners();
                redrawCanvas();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błędne parametry!", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateSelectedShape(String params) {
        if (selectedShape != null) {
            try {
                selectedShape.setParameters(params);
                redrawCanvas();
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
            case BezierCurve bezier -> "BEZIER";
            case null, default -> null;
        };
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public void saveToFile(String filename) {
        if (selectedShape != null) {
            selectedShape.setSelected(false);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(shapes);
            JOptionPane.showMessageDialog(this, "Zapisano pomyślnie do pliku: " + filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (selectedShape != null) {
                selectedShape.setSelected(true);
            }
        }
    }

    public void loadFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            shapes = (List<Shape>) ois.readObject();
            selectedShape = null;
            tempBezierCurve = null;
            redrawCanvas();
            JOptionPane.showMessageDialog(this, "Wczytano pomyślnie z pliku: " + filename);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Błąd odczytu: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearAll() {
        shapes.clear();
        selectedShape = null;
        tempBezierCurve = null;
        redrawCanvas();
    }
}