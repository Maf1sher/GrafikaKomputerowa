package org.mafisher;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PolygonShape extends Shape {
    private List<Point> vertices;

    public PolygonShape() {
        super();
        this.vertices = new ArrayList<>();
    }

    public PolygonShape(List<Point> vertices) {
        super();
        this.vertices = new ArrayList<>();
        for (Point p : vertices) {
            this.vertices.add(new Point(p));
        }
    }

    public void addVertex(Point p) {
        vertices.add(new Point(p));
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (vertices.size() < 2) return;

        g2d.setColor(color);
        int[] xPoints = vertices.stream().mapToInt(p -> p.x).toArray();
        int[] yPoints = vertices.stream().mapToInt(p -> p.y).toArray();
        g2d.drawPolygon(xPoints, yPoints, vertices.size());

        if (selected) {
            g2d.setColor(Color.BLUE);
            for (Point handle : getHandles()) {
                g2d.fillRect(handle.x - 3, handle.y - 3, 6, 6);
            }
            Point c = getCenter();
            g2d.setColor(Color.RED);
            g2d.drawOval(c.x - 2, c.y - 2, 4, 4);
        }
    }

    @Override
    public boolean contains(Point p) {
        Polygon poly = new Polygon();
        for (Point v : vertices) {
            poly.addPoint(v.x, v.y);
        }
        return poly.contains(p);
    }

    @Override
    public void move(int dx, int dy) {
        applyTransform(Matrix3x3.translation(dx, dy));
    }

    @Override
    public void resize(Point p, int handle) {
        if (handle >= 0 && handle < vertices.size()) {
            vertices.set(handle, new Point(p));
        }
    }

    @Override
    public List<Point> getHandles() {
        return new ArrayList<>(vertices);
    }

    @Override
    public int getHandleAt(Point p) {
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).distance(p) < 6) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getParameters() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertices.size(); i++) {
            sb.append(vertices.get(i).x).append(",").append(vertices.get(i).y);
            if (i < vertices.size() - 1) sb.append(";");
        }
        return sb.toString();
    }

    @Override
    public void setParameters(String params) {
        vertices.clear();
        String[] parts = params.split(";");
        for (String part : parts) {
            String[] coords = part.split(",");
            if (coords.length == 2) {
                vertices.add(new Point(
                        Integer.parseInt(coords[0].trim()),
                        Integer.parseInt(coords[1].trim())
                ));
            }
        }
    }

    @Override
    public void applyTransform(Matrix3x3 transform) {
        for (int i = 0; i < vertices.size(); i++) {
            vertices.set(i, transform.transform(vertices.get(i)));
        }
    }

    @Override
    public Point getCenter() {
        if (vertices.isEmpty()) return new Point(0,0);
        int sx = 0, sy = 0;
        for (Point p : vertices) {
            sx += p.x;
            sy += p.y;
        }
        return new Point(sx / vertices.size(), sy / vertices.size());
    }
}