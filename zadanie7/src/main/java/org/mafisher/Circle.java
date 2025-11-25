package org.mafisher;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Circle extends Shape {
    private Point center;
    private int radius;

    public Circle(Point center, int radius) {
        super();
        this.center = center;
        this.radius = radius;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2);

        if (selected) {
            g2d.setColor(Color.BLUE);
            for (Point handle : getHandles()) {
                g2d.fillRect(handle.x - 3, handle.y - 3, 6, 6);
            }
            g2d.setColor(Color.RED);
            g2d.drawOval(center.x - 1, center.y - 1, 2, 2);
        }
    }

    @Override
    public boolean contains(Point p) {
        return center.distance(p) <= radius;
    }

    @Override
    public void move(int dx, int dy) {
        applyTransform(Matrix3x3.translation(dx, dy));
    }

    @Override
    public void resize(Point p, int handle) {
        radius = (int) center.distance(p);
    }

    @Override
    public List<Point> getHandles() {
        List<Point> handles = new ArrayList<>();
        handles.add(new Point(center.x + radius, center.y));
        handles.add(new Point(center.x, center.y + radius));
        handles.add(new Point(center.x - radius, center.y));
        handles.add(new Point(center.x, center.y - radius));
        return handles;
    }

    @Override
    public int getHandleAt(Point p) {
        List<Point> handles = getHandles();
        for (int i = 0; i < handles.size(); i++) {
            if (handles.get(i).distance(p) < 5) return i;
        }
        return -1;
    }

    @Override
    public String getParameters() {
        return String.format("%d,%d,%d", center.x, center.y, radius);
    }

    @Override
    public void setParameters(String params) {
        String[] parts = params.split(",");
        if (parts.length == 3) {
            center = new Point(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
            radius = Integer.parseInt(parts[2].trim());
        }
    }

    @Override
    public void applyTransform(Matrix3x3 transform) {
        Point oldCenter = new Point(center);
        center = transform.transform(center);

        Point rim = new Point(oldCenter.x + radius, oldCenter.y);
        Point newRim = transform.transform(rim);
        radius = (int) center.distance(newRim);
    }

    @Override
    public Point getCenter() {
        return center;
    }
}