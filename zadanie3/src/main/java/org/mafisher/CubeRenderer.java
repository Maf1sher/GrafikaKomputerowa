package org.mafisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CubeRenderer extends JPanel {
    private float rotationX = 0.3f;
    private float rotationY = 0.5f;
    private int lastMouseX, lastMouseY;
    private boolean showCrossSection = false;
    private int crossSectionAxis = 0;
    private float crossSectionPosition = 0.5f;

    public CubeRenderer() {
        setBackground(Color.WHITE);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;

                rotationY += dx * 0.01f;
                rotationX += dy * 0.01f;

                lastMouseX = e.getX();
                lastMouseY = e.getY();
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public void setCrossSectionAxis(int axis) {
        this.crossSectionAxis = axis;
        repaint();
    }

    public void setCrossSectionPosition(float position) {
        this.crossSectionPosition = position;
        repaint();
    }

    public void setShowCrossSection(boolean show) {
        this.showCrossSection = show;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        float scale = Math.min(getWidth(), getHeight()) * 0.3f;

        List<Face> faces = generateCubeFaces(scale);
        List<Face> allFaces = new ArrayList<>();

        float cutPosition = crossSectionPosition;

        for (Face face : faces) {
            if (showCrossSection) {
                List<Face> clippedFaces = clipFaceBeforeTransform(face, cutPosition, scale);
                allFaces.addAll(clippedFaces);
            } else {
                allFaces.add(face);
            }
        }

        for (Face face : allFaces) {
            face.transform(rotationX, rotationY);
            face.translate(centerX, centerY);
        }

        allFaces.sort((f1, f2) -> Float.compare(f2.getAverageZ(), f1.getAverageZ()));

        for (Face face : allFaces) {
            face.draw(g2d);
        }

        if (showCrossSection) {
            drawCrossSection(g2d, centerX, centerY, scale);
        }
    }

    private List<Face> clipFaceBeforeTransform(Face face, float cutPosition, float scale) {
        List<Face> result = new ArrayList<>();
        Point3D[] vertices = face.getOriginalVertices();
        Color[] colors = face.getColors();

        float threshold = (cutPosition * 2 - 1) * scale;

        float[] coords = new float[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            switch (crossSectionAxis) {
                case 0: coords[i] = vertices[i].x; break;
                case 1: coords[i] = vertices[i].y; break;
                case 2: coords[i] = vertices[i].z; break;
            }
        }

        boolean allAbove = true;
        boolean allBelow = true;
        for (float coord : coords) {
            if (coord <= threshold) allAbove = false;
            if (coord >= threshold) allBelow = false;
        }

        if (allBelow) {
            result.add(face);
            return result;
        }

        if (allAbove) {
            return result;
        }

        List<Point3D> clippedVertices = new ArrayList<>();
        List<Color> clippedColors = new ArrayList<>();

        for (int i = 0; i < vertices.length; i++) {
            int next = (i + 1) % vertices.length;

            boolean currentBelow = coords[i] <= threshold;
            boolean nextBelow = coords[next] <= threshold;

            if (currentBelow) {
                clippedVertices.add(vertices[i].copy());
                clippedColors.add(colors[i]);
            }

            if (currentBelow != nextBelow) {
                float t = (threshold - coords[i]) / (coords[next] - coords[i]);
                Point3D intersection = interpolatePoint(vertices[i], vertices[next], t);
                Color intersectionColor = interpolateColor(colors[i], colors[next], t);
                clippedVertices.add(intersection);
                clippedColors.add(intersectionColor);
            }
        }

        if (clippedVertices.size() >= 3) {
            Point3D[] vertArray = clippedVertices.toArray(new Point3D[0]);
            Color[] colorArray = clippedColors.toArray(new Color[0]);
            result.add(new Face(vertArray, colorArray));
        }

        return result;
    }

    private Point3D interpolatePoint(Point3D p1, Point3D p2, float t) {
        return new Point3D(
                p1.x + (p2.x - p1.x) * t,
                p1.y + (p2.y - p1.y) * t,
                p1.z + (p2.z - p1.z) * t
        );
    }

    private Color interpolateColor(Color c1, Color c2, float t) {
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * t);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b))
        );
    }

    private List<Face> generateCubeFaces(float size) {
        List<Face> faces = new ArrayList<>();
        int resolution = 15;

        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                float u1 = i / (float) resolution;
                float u2 = (i + 1) / (float) resolution;
                float v1 = j / (float) resolution;
                float v2 = (j + 1) / (float) resolution;

                faces.add(createFace(0, u1, v1, u2, v2, size));
                faces.add(createFace(1, u1, v1, u2, v2, size));
                faces.add(createFace(2, u1, v1, u2, v2, size));
                faces.add(createFace(3, u1, v1, u2, v2, size));
                faces.add(createFace(4, u1, v1, u2, v2, size));
                faces.add(createFace(5, u1, v1, u2, v2, size));
            }
        }

        return faces;
    }

    private Face createFace(int faceIndex, float u1, float v1, float u2, float v2, float size) {
        Point3D[] vertices = new Point3D[4];
        Color[] colors = new Color[4];

        switch (faceIndex) {
            case 0:
                vertices[0] = new Point3D(size, u1 * 2 * size - size, v1 * 2 * size - size);
                vertices[1] = new Point3D(size, u2 * 2 * size - size, v1 * 2 * size - size);
                vertices[2] = new Point3D(size, u2 * 2 * size - size, v2 * 2 * size - size);
                vertices[3] = new Point3D(size, u1 * 2 * size - size, v2 * 2 * size - size);
                colors[0] = new Color(1, u1, v1);
                colors[1] = new Color(1, u2, v1);
                colors[2] = new Color(1, u2, v2);
                colors[3] = new Color(1, u1, v2);
                break;
            case 1:
                vertices[0] = new Point3D(-size, u1 * 2 * size - size, v1 * 2 * size - size);
                vertices[1] = new Point3D(-size, u2 * 2 * size - size, v1 * 2 * size - size);
                vertices[2] = new Point3D(-size, u2 * 2 * size - size, v2 * 2 * size - size);
                vertices[3] = new Point3D(-size, u1 * 2 * size - size, v2 * 2 * size - size);
                colors[0] = new Color(0, u1, v1);
                colors[1] = new Color(0, u2, v1);
                colors[2] = new Color(0, u2, v2);
                colors[3] = new Color(0, u1, v2);
                break;
            case 2:
                vertices[0] = new Point3D(u1 * 2 * size - size, size, v1 * 2 * size - size);
                vertices[1] = new Point3D(u2 * 2 * size - size, size, v1 * 2 * size - size);
                vertices[2] = new Point3D(u2 * 2 * size - size, size, v2 * 2 * size - size);
                vertices[3] = new Point3D(u1 * 2 * size - size, size, v2 * 2 * size - size);
                colors[0] = new Color(u1, 1, v1);
                colors[1] = new Color(u2, 1, v1);
                colors[2] = new Color(u2, 1, v2);
                colors[3] = new Color(u1, 1, v2);
                break;
            case 3:
                vertices[0] = new Point3D(u1 * 2 * size - size, -size, v1 * 2 * size - size);
                vertices[1] = new Point3D(u2 * 2 * size - size, -size, v1 * 2 * size - size);
                vertices[2] = new Point3D(u2 * 2 * size - size, -size, v2 * 2 * size - size);
                vertices[3] = new Point3D(u1 * 2 * size - size, -size, v2 * 2 * size - size);
                colors[0] = new Color(u1, 0, v1);
                colors[1] = new Color(u2, 0, v1);
                colors[2] = new Color(u2, 0, v2);
                colors[3] = new Color(u1, 0, v2);
                break;
            case 4:
                vertices[0] = new Point3D(u1 * 2 * size - size, v1 * 2 * size - size, size);
                vertices[1] = new Point3D(u2 * 2 * size - size, v1 * 2 * size - size, size);
                vertices[2] = new Point3D(u2 * 2 * size - size, v2 * 2 * size - size, size);
                vertices[3] = new Point3D(u1 * 2 * size - size, v2 * 2 * size - size, size);
                colors[0] = new Color(u1, v1, 1);
                colors[1] = new Color(u2, v1, 1);
                colors[2] = new Color(u2, v2, 1);
                colors[3] = new Color(u1, v2, 1);
                break;
            case 5:
                vertices[0] = new Point3D(u1 * 2 * size - size, v1 * 2 * size - size, -size);
                vertices[1] = new Point3D(u2 * 2 * size - size, v1 * 2 * size - size, -size);
                vertices[2] = new Point3D(u2 * 2 * size - size, v2 * 2 * size - size, -size);
                vertices[3] = new Point3D(u1 * 2 * size - size, v2 * 2 * size - size, -size);
                colors[0] = new Color(u1, v1, 0);
                colors[1] = new Color(u2, v1, 0);
                colors[2] = new Color(u2, v2, 0);
                colors[3] = new Color(u1, v2, 0);
                break;
        }

        return new Face(vertices, colors);
    }

    private void drawCrossSection(Graphics2D g2d, int centerX, int centerY, float scale) {
        List<Face> crossSectionFaces = new ArrayList<>();
        int resolution = 15;

        float pos = (crossSectionPosition * 2 - 1) * scale;

        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                float u1 = i / (float) resolution * 2 - 1;
                float u2 = (i + 1) / (float) resolution * 2 - 1;
                float v1 = j / (float) resolution * 2 - 1;
                float v2 = (j + 1) / (float) resolution * 2 - 1;

                Point3D[] vertices = new Point3D[4];
                Color[] colors = new Color[4];

                float colorU1 = (u1 + 1) / 2;
                float colorU2 = (u2 + 1) / 2;
                float colorV1 = (v1 + 1) / 2;
                float colorV2 = (v2 + 1) / 2;

                switch (crossSectionAxis) {
                    case 0:
                        vertices[0] = new Point3D(pos, u1 * scale, v1 * scale);
                        vertices[1] = new Point3D(pos, u2 * scale, v1 * scale);
                        vertices[2] = new Point3D(pos, u2 * scale, v2 * scale);
                        vertices[3] = new Point3D(pos, u1 * scale, v2 * scale);
                        colors[0] = new Color(crossSectionPosition, colorU1, colorV1);
                        colors[1] = new Color(crossSectionPosition, colorU2, colorV1);
                        colors[2] = new Color(crossSectionPosition, colorU2, colorV2);
                        colors[3] = new Color(crossSectionPosition, colorU1, colorV2);
                        break;
                    case 1:
                        vertices[0] = new Point3D(u1 * scale, pos, v1 * scale);
                        vertices[1] = new Point3D(u2 * scale, pos, v1 * scale);
                        vertices[2] = new Point3D(u2 * scale, pos, v2 * scale);
                        vertices[3] = new Point3D(u1 * scale, pos, v2 * scale);
                        colors[0] = new Color(colorU1, crossSectionPosition, colorV1);
                        colors[1] = new Color(colorU2, crossSectionPosition, colorV1);
                        colors[2] = new Color(colorU2, crossSectionPosition, colorV2);
                        colors[3] = new Color(colorU1, crossSectionPosition, colorV2);
                        break;
                    case 2:
                        vertices[0] = new Point3D(u1 * scale, v1 * scale, pos);
                        vertices[1] = new Point3D(u2 * scale, v1 * scale, pos);
                        vertices[2] = new Point3D(u2 * scale, v2 * scale, pos);
                        vertices[3] = new Point3D(u1 * scale, v2 * scale, pos);
                        colors[0] = new Color(colorU1, colorV1, crossSectionPosition);
                        colors[1] = new Color(colorU2, colorV1, crossSectionPosition);
                        colors[2] = new Color(colorU2, colorV2, crossSectionPosition);
                        colors[3] = new Color(colorU1, colorV2, crossSectionPosition);
                        break;
                }

                Face face = new Face(vertices, colors);
                face.transform(rotationX, rotationY);
                face.translate(centerX, centerY);
                crossSectionFaces.add(face);
            }
        }

        for (Face face : crossSectionFaces) {
            face.draw(g2d);
        }
    }
}