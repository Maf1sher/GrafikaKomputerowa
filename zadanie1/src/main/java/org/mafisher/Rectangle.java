package org.mafisher;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Rectangle extends Shape {
    private Point topLeft;
    private int width, height;

    public Rectangle(Point topLeft, int width, int height) {
        super();
        this.topLeft = topLeft;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);

        int drawX = width >= 0 ? topLeft.x : topLeft.x + width;
        int drawY = height >= 0 ? topLeft.y : topLeft.y + height;
        int drawWidth = Math.abs(width);
        int drawHeight = Math.abs(height);

        g2d.drawRect(drawX, drawY, drawWidth, drawHeight);

        if (selected) {
            g2d.setColor(Color.BLUE);
            for (Point handle : getHandles()) {
                g2d.fillRect(handle.x - 3, handle.y - 3, 6, 6);
            }
        }
    }

    @Override
    public boolean contains(Point p) {
        int x1 = width >= 0 ? topLeft.x : topLeft.x + width;
        int y1 = height >= 0 ? topLeft.y : topLeft.y + height;
        int w = Math.abs(width);
        int h = Math.abs(height);

        return p.x >= x1 && p.x <= x1 + w &&
                p.y >= y1 && p.y <= y1 + h;
    }

    @Override
    public void move(int dx, int dy) {
        topLeft.translate(dx, dy);
    }

    @Override
    public void resize(Point p, int handle) {
        switch (handle) {
            case 0: // Top-left
                width += topLeft.x - p.x;
                height += topLeft.y - p.y;
                topLeft = p;
                break;
            case 1: // Top-right
                width = p.x - topLeft.x;
                height += topLeft.y - p.y;
                topLeft.y = p.y;
                break;
            case 2: // Bottom-right
                width = p.x - topLeft.x;
                height = p.y - topLeft.y;
                break;
            case 3: // Bottom-left
                width += topLeft.x - p.x;
                topLeft.x = p.x;
                height = p.y - topLeft.y;
                break;
        }
    }

    @Override
    public List<Point> getHandles() {
        List<Point> handles = new ArrayList<>();
        handles.add(new Point(topLeft.x, topLeft.y));
        handles.add(new Point(topLeft.x + width, topLeft.y));
        handles.add(new Point(topLeft.x + width, topLeft.y + height));
        handles.add(new Point(topLeft.x, topLeft.y + height));
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
        return String.format("%d,%d,%d,%d", topLeft.x, topLeft.y, width, height);
    }

    @Override
    public void setParameters(String params) {
        String[] parts = params.split(",");
        if (parts.length == 4) {
            topLeft = new Point(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
            width = Integer.parseInt(parts[2].trim());
            height = Integer.parseInt(parts[3].trim());
        }
    }
}