package org.mafisher;

import java.awt.*;
import java.awt.geom.Path2D;

public class Face {
    private Point3D[] originalVertices;
    private Point3D[] transformedVertices;
    private Color[] colors;

    public Face(Point3D[] vertices, Color[] colors) {
        this.originalVertices = new Point3D[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.originalVertices[i] = vertices[i].copy();
        }
        this.transformedVertices = vertices;
        this.colors = colors;
    }

    public void transform(float rotationX, float rotationY) {
        transformedVertices = new Point3D[originalVertices.length];
        for (int i = 0; i < originalVertices.length; i++) {
            transformedVertices[i] = originalVertices[i].copy();
            transformedVertices[i].rotateX(rotationX);
            transformedVertices[i].rotateY(rotationY);
        }
    }

    public void translate(float dx, float dy) {
        for (Point3D vertex : transformedVertices) {
            vertex.translate(dx, dy);
        }
    }

    public float getAverageZ() {
        float sum = 0;
        for (Point3D vertex : transformedVertices) {
            sum += vertex.z;
        }
        return sum / transformedVertices.length;
    }

    public Point3D[] getOriginalVertices() {
        return originalVertices;
    }

    public Point3D[] getTransformedVertices() {
        return transformedVertices;
    }

    public Color[] getColors() {
        return colors;
    }

    public void draw(Graphics2D g2d) {
        if (transformedVertices.length < 3) return;

        Path2D path = new Path2D.Float();
        path.moveTo(transformedVertices[0].getScreenX(), transformedVertices[0].getScreenY());
        for (int i = 1; i < transformedVertices.length; i++) {
            path.lineTo(transformedVertices[i].getScreenX(), transformedVertices[i].getScreenY());
        }
        path.closePath();

        Color avgColor = getAverageColor();
        g2d.setColor(avgColor);
        g2d.fill(path);

        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.draw(path);
    }

    private Color getAverageColor() {
        float r = 0, g = 0, b = 0;
        for (Color color : colors) {
            r += color.getRed();
            g += color.getGreen();
            b += color.getBlue();
        }
        r /= colors.length;
        g /= colors.length;
        b /= colors.length;
        return new Color((int) r, (int) g, (int) b);
    }
}
