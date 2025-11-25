package org.mafisher;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

public abstract class Shape implements Serializable {
    protected Color color;
    protected boolean selected;

    public Shape() {
        this.color = Color.BLACK;
        this.selected = false;
    }

    public abstract void draw(Graphics2D g2d);
    public abstract boolean contains(Point p);

    public abstract void move(int dx, int dy);

    public abstract void resize(Point p, int handle);
    public abstract List<Point> getHandles();
    public abstract int getHandleAt(Point p);
    public abstract String getParameters();
    public abstract void setParameters(String params);

    public abstract void applyTransform(Matrix3x3 transform);
    public abstract Point getCenter();

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}