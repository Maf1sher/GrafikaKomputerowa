package org.mafisher;

import java.util.ArrayList;
import java.util.List;

public class ColorModel {
    private float r, g, b;
    private float c, m, y, k;
    private List<ColorObserver> observers = new ArrayList<>();
    private boolean updating = false;

    public ColorModel() {
        r = g = b = 0.5f;
        updateCMYKFromRGB();
    }

    public void addObserver(ColorObserver observer) {
        observers.add(observer);
    }

    public void setRGB(float r, float g, float b) {
        if (updating) return;
        updating = true;
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        updateCMYKFromRGB();
        notifyObservers();
        updating = false;
    }

    public void setCMYK(float c, float m, float y, float k) {
        if (updating) return;
        updating = true;
        this.c = clamp(c);
        this.m = clamp(m);
        this.y = clamp(y);
        this.k = clamp(k);
        updateRGBFromCMYK();
        notifyObservers();
        updating = false;
    }

    private void updateCMYKFromRGB() {
        k = Math.min(1 - r, Math.min(1 - g, 1 - b));
        if (k < 1.0f) {
            c = (1 - r - k) / (1 - k);
            m = (1 - g - k) / (1 - k);
            y = (1 - b - k) / (1 - k);
        } else {
            c = m = y = 0;
        }
    }

    private void updateRGBFromCMYK() {
        r = 1 - Math.min(1, c * (1 - k) + k);
        g = 1 - Math.min(1, m * (1 - k) + k);
        b = 1 - Math.min(1, y * (1 - k) + k);
    }

    private float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    private void notifyObservers() {
        for (ColorObserver observer : observers) {
            observer.onColorChanged();
        }
    }

    public float getR() { return r; }
    public float getG() { return g; }
    public float getB() { return b; }
    public float getC() { return c; }
    public float getM() { return m; }
    public float getY() { return y; }
    public float getK() { return k; }
}
