package org.mafisher;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BezierCurve extends Shape {
    private List<Point> controlPoints;
    private int degree;

    public BezierCurve(int degree) {
        super();
        this.degree = degree;
        this.controlPoints = new ArrayList<>();
    }

    public void addControlPoint(Point p) {
        if (controlPoints.size() < degree + 1) {
            controlPoints.add(new Point(p));
        }
    }

    public boolean isComplete() { return controlPoints.size() == degree + 1; }
    public int getDegree() { return degree; }

    @Override
    public void draw(Graphics2D g2d) {
        if (controlPoints.size() < 2) return;

        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            Point p1 = controlPoints.get(i);
            Point p2 = controlPoints.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        if (isComplete()) {
            g2d.setColor(color);
            Point prevPoint = calculateBezierPoint(0.0);
            for (double t = 0.01; t <= 1.0; t += 0.01) {
                Point currentPoint = calculateBezierPoint(t);
                g2d.drawLine(prevPoint.x, prevPoint.y, currentPoint.x, currentPoint.y);
                prevPoint = currentPoint;
            }
        }

        if(selected){
            for (Point p : controlPoints) {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(p.x - 4, p.y - 4, 8, 8);
            }
        }
    }

    private Point calculateBezierPoint(double t) {
        int n = controlPoints.size() - 1;
        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i <= n; i++) {
            double basis = bernsteinPolynomial(n, i, t);
            x += basis * controlPoints.get(i).x;
            y += basis * controlPoints.get(i).y;
        }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    private double bernsteinPolynomial(int n, int i, double t) {
        return binomialCoefficient(n, i) * Math.pow(1 - t, n - i) * Math.pow(t, i);
    }

    private long binomialCoefficient(int n, int k) {
        if (k < 0 || k > n) return 0;
        if (k == 0 || k == n) return 1;
        long result = 1;
        for (int i = 0; i < k; i++) result = result * (n - i) / (i + 1);
        return result;
    }

    @Override
    public boolean contains(Point p) {
        if (!isComplete()) return false;
        Point prevPoint = calculateBezierPoint(0.0);
        for (double t = 0.01; t <= 1.0; t += 0.01) {
            Point currentPoint = calculateBezierPoint(t);
            if(distanceToSegment(p, prevPoint, currentPoint) < 5) return true;
            prevPoint = currentPoint;
        }
        return false;
    }

    private double distanceToSegment(Point p, Point p1, Point p2) {
        double A = p.x - p1.x;
        double B = p.y - p1.y;
        double C = p2.x - p1.x;
        double D = p2.y - p1.y;
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? dot / lenSq : -1;
        double xx, yy;
        if (param < 0) { xx = p1.x; yy = p1.y; }
        else if (param > 1) { xx = p2.x; yy = p2.y; }
        else { xx = p1.x + param * C; yy = p1.y + param * D; }
        double dx = p.x - xx;
        double dy = p.y - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void move(int dx, int dy) {
        applyTransform(Matrix3x3.translation(dx, dy));
    }

    @Override
    public void resize(Point p, int handle) {
        if (handle >= 0 && handle < controlPoints.size()) {
            controlPoints.set(handle, new Point(p));
        }
    }

    @Override
    public List<Point> getHandles() {
        return new ArrayList<>(controlPoints);
    }

    @Override
    public int getHandleAt(Point p) {
        for (int i = 0; i < controlPoints.size(); i++) {
            if (controlPoints.get(i).distance(p) < 12) return i;
        }
        return -1;
    }

    @Override
    public String getParameters() {
        StringBuilder sb = new StringBuilder();
        sb.append(degree).append(";");
        for (int i = 0; i < controlPoints.size(); i++) {
            Point p = controlPoints.get(i);
            sb.append(p.x).append(",").append(p.y);
            if (i < controlPoints.size() - 1) sb.append(";");
        }
        return sb.toString();
    }

    @Override
    public void setParameters(String params) {
        String[] parts = params.split(";");
        if (parts.length >= 2) {
            degree = Integer.parseInt(parts[0].trim());
            controlPoints.clear();
            for (int i = 1; i < parts.length; i++) {
                String[] coords = parts[i].split(",");
                if (coords.length == 2) {
                    controlPoints.add(new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
                }
            }
        }
    }

    @Override
    public void applyTransform(Matrix3x3 transform) {
        for (int i = 0; i < controlPoints.size(); i++) {
            controlPoints.set(i, transform.transform(controlPoints.get(i)));
        }
    }

    @Override
    public Point getCenter() {
        if (controlPoints.isEmpty()) return new Point(0,0);
        int sx=0, sy=0;
        for(Point p : controlPoints){ sx+=p.x; sy+=p.y; }
        return new Point(sx/controlPoints.size(), sy/controlPoints.size());
    }
}