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
            for (Point handle : getHandles()) {
                g2d.fillRect(handle.x - 3, handle.y - 3, 6, 6);
            }
        }
    }

    @Override
    public boolean contains(Point p) {
        Line2D line = new Line2D.Double(p1, p2);
        return line.ptSegDist(p) < 5;
    }

    @Override
    public void move(int dx, int dy) {
        p1.translate(dx, dy);
        p2.translate(dx, dy);
    }

    @Override
    public void resize(Point p, int handle) {
        if (handle == 0) {
            p1 = p;
        } else if (handle == 1) {
            p2 = p;
        }
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
        List<Point> handles = getHandles();
        for (int i = 0; i < handles.size(); i++) {
            if (handles.get(i).distance(p) < 5) {
                return i;
            }
        }
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
            p1 = new Point(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
            p2 = new Point(Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[3].trim()));
        }
    }
}