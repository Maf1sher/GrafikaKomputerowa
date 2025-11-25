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
    private String interactionMode;

    private Shape selectedShape;
    private int selectedHandle = -1;

    private Point lastMousePoint;
    private Point startDragPoint;
    private Point currentPivot;

    private boolean draggingBody;
    private boolean resizingHandle;
    private boolean creating;

    private BufferedImage canvas;
    private Graphics2D canvasGraphics;

    private BezierCurve tempBezierCurve;
    private PolygonShape tempPolygon;
    private int bezierDegree = 2;

    private final List<ShapeChangeListener> listeners;

    public DrawingPanel() {
        shapes = new ArrayList<>();
        listeners = new ArrayList<>();
        currentTool = "SELECT";
        interactionMode = "MOVE";
        setBackground(Color.WHITE);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleMousePressed(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handleMouseReleased(e); }
            @Override
            public void mouseClicked(MouseEvent e) { handleMouseClicked(e); }
        };

        addMouseListener(ma);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) { handleMouseDragged(e); }
            @Override
            public void mouseMoved(MouseEvent e) { handleMouseMoved(e); }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) { initializeCanvas(); }
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
        if (canvas == null || canvasGraphics == null) return;

        canvasGraphics.setColor(Color.WHITE);
        canvasGraphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Shape shape : shapes) {
            shape.draw(canvasGraphics);
        }

        if (tempBezierCurve != null) tempBezierCurve.draw(canvasGraphics);

        if (tempPolygon != null) {
            tempPolygon.draw(canvasGraphics);
            if (lastMousePoint != null && !tempPolygon.getHandles().isEmpty()) {
                Point p = tempPolygon.getHandles().get(tempPolygon.getHandles().size()-1);
                canvasGraphics.setColor(Color.LIGHT_GRAY);
                canvasGraphics.drawLine(p.x, p.y, lastMousePoint.x, lastMousePoint.y);
            }
        }

        if (selectedShape != null && currentPivot != null) {
            drawPivot(canvasGraphics, currentPivot);
        }

        repaint();
    }

    private void drawPivot(Graphics2D g, Point p) {
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(1.5f));
        int size = 6;
        g.drawLine(p.x - size, p.y, p.x + size, p.y);
        g.drawLine(p.x, p.y - size, p.x, p.y + size);
        g.drawOval(p.x - 3, p.y - 3, 6, 6);
        g.setStroke(new BasicStroke(1.0f));
    }

    public void addShapeChangeListener(ShapeChangeListener listener) { listeners.add(listener); }
    private void notifyListeners() {
        for (ShapeChangeListener listener : listeners) listener.onShapeChanged(this.selectedShape);
    }

    private void setSelectedShape(Shape shape) {
        if (selectedShape != null) selectedShape.setSelected(false);
        selectedShape = shape;
        if (selectedShape != null) {
            selectedShape.setSelected(true);
            currentPivot = selectedShape.getCenter();
        } else {
            currentPivot = null;
        }
    }

    public void setCustomPivot(Point p) {
        this.currentPivot = p;
        redrawCanvas();
    }

    public Point getCurrentPivot() { return currentPivot; }

    private void handleMouseClicked(MouseEvent e) {
        if (currentTool.equals("POLYGON")) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (tempPolygon == null) tempPolygon = new PolygonShape();
                tempPolygon.addVertex(e.getPoint());
                if (e.getClickCount() == 2 && tempPolygon.getHandles().size() >= 3) {
                    shapes.add(tempPolygon);
                    setSelectedShape(tempPolygon);
                    tempPolygon = null;

                    setCurrentTool("SELECT");
                    notifyListeners();
                }
                redrawCanvas();
            } else if (e.getButton() == MouseEvent.BUTTON3 && tempPolygon != null) {
                if (tempPolygon.getHandles().size() >= 3) {
                    shapes.add(tempPolygon);
                    setSelectedShape(tempPolygon);

                    setCurrentTool("SELECT");
                    notifyListeners();
                }
                tempPolygon = null;
                redrawCanvas();
            }
        }
    }

    private void handleMousePressed(MouseEvent e) {
        Point p = e.getPoint();
        lastMousePoint = p;
        startDragPoint = p;
        draggingBody = false;
        resizingHandle = false;

        if (currentTool.equals("SELECT")) {
            if (selectedShape != null) {
                selectedHandle = selectedShape.getHandleAt(p);
                if (selectedHandle != -1) {
                    resizingHandle = true;
                    notifyListeners();
                    redrawCanvas();
                    return;
                }
            }

            boolean found = false;
            for (int i = shapes.size() - 1; i >= 0; i--) {
                if (shapes.get(i).contains(p)) {
                    setSelectedShape(shapes.get(i));
                    found = true;
                    draggingBody = true;
                    break;
                }
            }

            if (!found) {
                setSelectedShape(null);
            }

            notifyListeners();
            redrawCanvas();

        } else if (currentTool.equals("BEZIER")) {
            if (tempBezierCurve == null) tempBezierCurve = new BezierCurve(bezierDegree);
            tempBezierCurve.addControlPoint(p);
            if (tempBezierCurve.isComplete()) {
                shapes.add(tempBezierCurve);
                setSelectedShape(tempBezierCurve);
                tempBezierCurve = null;

                setCurrentTool("SELECT");
                notifyListeners();
            }
            redrawCanvas();
        } else if (!currentTool.equals("POLYGON")) {
            creating = true;
            Shape shape = switch (currentTool) {
                case "LINE" -> new Line(p, p);
                case "RECTANGLE" -> new Rectangle(p, 0, 0);
                case "CIRCLE" -> new Circle(p, 0);
                default -> null;
            };

            if (shape != null) {
                setSelectedShape(shape);
                shapes.add(shape);
                redrawCanvas();
            }
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        Point currentPoint = e.getPoint();

        if (creating && selectedShape != null) {
            if (selectedShape instanceof Rectangle) {
                int x = Math.min(startDragPoint.x, currentPoint.x);
                int y = Math.min(startDragPoint.y, currentPoint.y);
                int w = Math.abs(startDragPoint.x - currentPoint.x);
                int h = Math.abs(startDragPoint.y - currentPoint.y);
                ((Rectangle)selectedShape).setFromBounds(x, y, w, h);
            } else if (selectedShape instanceof Line) {
                selectedShape.resize(currentPoint, 1);
            } else {
                selectedShape.resize(currentPoint, 1);
            }
            redrawCanvas();
            return;
        }

        if (currentTool.equals("SELECT") && selectedShape != null) {

            if (resizingHandle && selectedHandle != -1) {
                selectedShape.resize(currentPoint, selectedHandle);
                currentPivot = selectedShape.getCenter();
            }
            else if (draggingBody) {
                if (interactionMode.equals("ROTATE")) {
                    if (currentPivot == null) currentPivot = selectedShape.getCenter();
                    double angle1 = Math.atan2(lastMousePoint.y - currentPivot.y, lastMousePoint.x - currentPivot.x);
                    double angle2 = Math.atan2(currentPoint.y - currentPivot.y, currentPoint.x - currentPivot.x);
                    double deltaAngle = Math.toDegrees(angle2 - angle1);
                    selectedShape.applyTransform(Matrix3x3.rotation(deltaAngle, currentPivot));
                } else if (interactionMode.equals("SCALE")) {
                    if (currentPivot == null) currentPivot = selectedShape.getCenter();
                    double dist1 = currentPivot.distance(lastMousePoint);
                    double dist2 = currentPivot.distance(currentPoint);
                    if (dist1 > 1.0) {
                        double scale = dist2 / dist1;
                        selectedShape.applyTransform(Matrix3x3.scaling(scale, currentPivot));
                    }
                } else {
                    int dx = currentPoint.x - lastMousePoint.x;
                    int dy = currentPoint.y - lastMousePoint.y;
                    selectedShape.applyTransform(Matrix3x3.translation(dx, dy));
                    if (currentPivot != null) currentPivot.translate(dx, dy);
                }
            }

            lastMousePoint = currentPoint;
            redrawCanvas();
            notifyListeners();
        }
    }

    private void handleMouseMoved(MouseEvent e) {
        if (currentTool.equals("POLYGON") && tempPolygon != null) {
            lastMousePoint = e.getPoint();
            redrawCanvas();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (creating) {
            creating = false;
            setCurrentTool("SELECT");
        }

        draggingBody = false;
        resizingHandle = false;
        redrawCanvas();
        notifyListeners();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas == null) initializeCanvas();
        g.drawImage(canvas, 0, 0, null);
    }

    public void setCurrentTool(String tool) {
        this.currentTool = tool;
        if (!tool.equals("SELECT")) {
            setSelectedShape(null);
            tempBezierCurve = null;
            tempPolygon = null;
        }
        redrawCanvas();
    }

    public void setInteractionMode(String mode) {
        this.interactionMode = mode;
        if (selectedShape != null) currentPivot = selectedShape.getCenter();
        redrawCanvas();
    }

    public void setBezierDegree(int degree) {
        this.bezierDegree = degree;
        tempBezierCurve = null;
    }

    public void applyTransformation(String type, double val1, double val2, double val3) {
        if (selectedShape == null) return;
        Matrix3x3 m = new Matrix3x3();
        Point center = (currentPivot != null) ? currentPivot : selectedShape.getCenter();
        switch(type) {
            case "TRANSLATE":
                m = Matrix3x3.translation(val1, val2);
                if(currentPivot != null) currentPivot.translate((int)val1, (int)val2);
                break;
            case "ROTATE":
                Point pivotRot = (val2 == -1 && val3 == -1) ? center : new Point((int)val2, (int)val3);
                currentPivot = pivotRot;
                m = Matrix3x3.rotation(val1, pivotRot);
                break;
            case "SCALE":
                Point pivotScale = (val2 == -1 && val3 == -1) ? center : new Point((int)val2, (int)val3);
                currentPivot = pivotScale;
                m = Matrix3x3.scaling(val1, pivotScale);
                break;
        }
        selectedShape.applyTransform(m);
        redrawCanvas();
        notifyListeners();
    }

    public void addShapeFromParameters(String type, String params) {
        try {
            Shape shape = switch (type) {
                case "LINE" -> new Line(new Point(0,0), new Point(0,0));
                case "RECTANGLE" -> new Rectangle(new Point(0,0), 0, 0);
                case "CIRCLE" -> new Circle(new Point(0,0), 0);
                case "POLYGON" -> new PolygonShape();
                case "BEZIER" -> new BezierCurve(2);
                default -> null;
            };
            if (shape != null) {
                shape.setParameters(params);
                shapes.add(shape);
                setSelectedShape(shape);
                notifyListeners();
                redrawCanvas();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Błędne parametry!"); }
    }

    public void updateSelectedShape(String params) {
        if (selectedShape != null) {
            try {
                selectedShape.setParameters(params);
                currentPivot = selectedShape.getCenter();
                redrawCanvas();
            } catch (Exception ex) { }
        }
    }

    public String getSelectedShapeParameters() { return selectedShape != null ? selectedShape.getParameters() : ""; }
    public String getSelectedShapeType() {
        return switch (selectedShape) {
            case Line l -> "LINE";
            case Rectangle r -> "RECTANGLE";
            case Circle c -> "CIRCLE";
            case BezierCurve b -> "BEZIER";
            case PolygonShape p -> "POLYGON";
            case null, default -> null;
        };
    }
    public Shape getSelectedShape() { return selectedShape; }

    public void saveToFile(String filename) {
        if (selectedShape != null) selectedShape.setSelected(false);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(shapes);
            JOptionPane.showMessageDialog(this, "Zapisano: " + filename);
        } catch (IOException e) { JOptionPane.showMessageDialog(this, "Błąd zapisu: " + e.getMessage());
        } finally {
            if (selectedShape != null) selectedShape.setSelected(true);
            redrawCanvas();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            shapes = (List<Shape>) ois.readObject();
            setSelectedShape(null);
            redrawCanvas();
            JOptionPane.showMessageDialog(this, "Wczytano: " + filename);
        } catch (IOException | ClassNotFoundException e) { JOptionPane.showMessageDialog(this, "Błąd odczytu: " + e.getMessage()); }
    }

    public void clearAll() {
        shapes.clear();
        setSelectedShape(null);
        redrawCanvas();
    }
}