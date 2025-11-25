package org.mafisher;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class Line extends Shape {
    private Point p1, p2;

    public Line(Point p1, Point p2) {
        super();
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        if (selected) {
            g2d.setColor(Color.BLUE);
            g2d.fillRect(p1.x - 3, p1.y - 3, 6, 6);
            g2d.fillRect(p2.x - 3, p2.y - 3, 6, 6);
        }
    }

    @Override
    public boolean contains(Point p) {
        Line2D line = new Line2D.Double(p1, p2);
        return line.ptSegDist(p) < 15;
    }

    @Override
    public void move(int dx, int dy) {
        applyTransform(Matrix3x3.translation(dx, dy));
    }

    @Override
    public void resize(Point p, int handle) {
        if (handle == 0) p1 = p;
        else if (handle == 1) p2 = p;
    }

    @Override
    public List<Point> getHandles() {
        List<Point> handles = new ArrayList<>();
        handles.add(p1);
        handles.add(p2);
        return handles;
    }

    @Override
    public int getHandleAt(Point p) {
        if (p1.distance(p) < 8) return 0;
        if (p2.distance(p) < 8) return 1;
        return -1;
    }

    @Override
    public String getParameters() {
        return String.format("%d,%d,%d,%d", p1.x, p1.y, p2.x, p2.y);
    }

    @Override
    public void setParameters(String params) {
        String[] parts = params.split(",");
        if (parts.length == 4) {
            p1 = new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            p2 = new Point(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        }
    }

    @Override
    public void applyTransform(Matrix3x3 transform) {
        p1 = transform.transform(p1);
        p2 = transform.transform(p2);
    }

    @Override
    public Point getCenter() {
        return new Point((p1.x + p2.x)/2, (p1.y + p2.y)/2);
    }
}