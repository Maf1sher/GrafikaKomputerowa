package org.mafisher;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Rectangle extends Shape {
    private List<Point> points;

    public Rectangle(Point topLeft, int width, int height) {
        super();
        setFromBounds(topLeft.x, topLeft.y, width, height);
    }

    public void setFromBounds(int x, int y, int width, int height) {
        points = new ArrayList<>();
        points.add(new Point(x, y));
        points.add(new Point(x + width, y));
        points.add(new Point(x + width, y + height));
        points.add(new Point(x, y + height));
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        int[] xPoints = points.stream().mapToInt(p -> p.x).toArray();
        int[] yPoints = points.stream().mapToInt(p -> p.y).toArray();
        g2d.drawPolygon(xPoints, yPoints, 4);

        if (selected) {
            g2d.setColor(Color.BLUE);
            for (Point handle : getHandles()) {
                g2d.fillRect(handle.x - 4, handle.y - 4, 8, 8);
            }
        }
    }

    @Override
    public boolean contains(Point p) {
        Polygon poly = new Polygon();
        for (Point pt : points) poly.addPoint(pt.x, pt.y);
        return poly.contains(p);
    }

    @Override
    public void move(int dx, int dy) {
        applyTransform(Matrix3x3.translation(dx, dy));
    }

    @Override
    public void resize(Point mouseP, int handle) {
        int oppositeIndex = (handle + 2) % 4;
        Point pivot = points.get(oppositeIndex);

        Point adj = points.get((oppositeIndex + 1) % 4);
        double angle = Math.atan2(adj.y - pivot.y, adj.x - pivot.x);

        int dx = mouseP.x - pivot.x;
        int dy = mouseP.y - pivot.y;

        double cos = Math.cos(-angle);
        double sin = Math.sin(-angle);

        double localX = dx * cos - dy * sin;
        double localY = dx * sin + dy * cos;

        Point p_pivot = new Point(0, 0);
        Point p_drag  = new Point((int)localX, (int)localY);
        Point p_adj1  = new Point((int)localX, 0);
        Point p_adj2  = new Point(0, (int)localY);

        Point[] newPoints = new Point[4];
        newPoints[oppositeIndex] = transformBack(p_pivot, pivot, angle);
        newPoints[handle]        = transformBack(p_drag, pivot, angle);

        newPoints[(oppositeIndex + 1) % 4] = transformBack(p_adj1, pivot, angle);
        newPoints[(oppositeIndex + 3) % 4] = transformBack(p_adj2, pivot, angle);

        for(int i=0; i<4; i++) points.set(i, newPoints[i]);
    }

    private Point transformBack(Point local, Point pivotOrigin, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        int x = (int)(local.x * cos - local.y * sin);
        int y = (int)(local.x * sin + local.y * cos);

        return new Point(x + pivotOrigin.x, y + pivotOrigin.y);
    }

    @Override
    public List<Point> getHandles() { return new ArrayList<>(points); }

    @Override
    public int getHandleAt(Point p) {
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).distance(p) < 12) return i;
        }
        return -1;
    }

    @Override
    public String getParameters() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<points.size(); i++){
            sb.append(points.get(i).x).append(",").append(points.get(i).y);
            if(i < points.size()-1) sb.append(";");
        }
        return sb.toString();
    }

    @Override
    public void setParameters(String params) {
        if (!params.contains(";")) {
            String[] parts = params.split(",");
            if (parts.length == 4) {
                setFromBounds(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            }
        } else {
            points.clear();
            String[] parts = params.split(";");
            for (String part : parts) {
                String[] coords = part.split(",");
                points.add(new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
            }
        }
    }

    @Override
    public void applyTransform(Matrix3x3 transform) {
        for (int i = 0; i < points.size(); i++) points.set(i, transform.transform(points.get(i)));
    }

    @Override
    public Point getCenter() {
        int sx=0, sy=0;
        for(Point p : points) { sx += p.x; sy += p.y; }
        return new Point(sx/points.size(), sy/points.size());
    }
}