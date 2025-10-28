package org.mafisher;

public class Point3D {
    public float x, y, z;

    public Point3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D copy() {
        return new Point3D(x, y, z);
    }

    public void rotateX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float newY = y * cos - z * sin;
        float newZ = y * sin + z * cos;
        y = newY;
        z = newZ;
    }

    public void rotateY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float newX = x * cos + z * sin;
        float newZ = -x * sin + z * cos;
        x = newX;
        z = newZ;
    }

    public void translate(float dx, float dy) {
        x += dx;
        y += dy;
    }

    public int getScreenX() {
        return (int) x;
    }

    public int getScreenY() {
        return (int) y;
    }
}