package org.mafisher;

import java.awt.Point;
import java.io.Serializable;

public class Matrix3x3 implements Serializable {
    private double[][] m;

    public Matrix3x3() {
        m = new double[3][3];
        identity();
    }

    public void identity() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                m[i][j] = (i == j) ? 1 : 0;
    }

    public static Matrix3x3 translation(double dx, double dy) {
        Matrix3x3 mat = new Matrix3x3();
        mat.m[0][0] = 1; mat.m[0][1] = 0; mat.m[0][2] = dx;
        mat.m[1][0] = 0; mat.m[1][1] = 1; mat.m[1][2] = dy;
        mat.m[2][0] = 0; mat.m[2][1] = 0; mat.m[2][2] = 1;
        return mat;
    }

    public static Matrix3x3 rotation(double angleDegrees, Point pivot) {
        double rad = Math.toRadians(angleDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        Matrix3x3 tOrigin = translation(-pivot.x, -pivot.y);

        Matrix3x3 rot = new Matrix3x3();
        rot.m[0][0] = cos;  rot.m[0][1] = -sin; rot.m[0][2] = 0;
        rot.m[1][0] = sin;  rot.m[1][1] = cos;  rot.m[1][2] = 0;
        rot.m[2][0] = 0;    rot.m[2][1] = 0;    rot.m[2][2] = 1;

        Matrix3x3 tBack = translation(pivot.x, pivot.y);

        return tBack.multiply(rot.multiply(tOrigin));
    }

    public static Matrix3x3 scaling(double scaleFactor, Point pivot) {
        Matrix3x3 tOrigin = translation(-pivot.x, -pivot.y);
        Matrix3x3 scale = new Matrix3x3();
        scale.m[0][0] = scaleFactor; scale.m[1][1] = scaleFactor; scale.m[2][2] = 1;
        Matrix3x3 tBack = translation(pivot.x, pivot.y);
        return tBack.multiply(scale.multiply(tOrigin));
    }

    public Matrix3x3 multiply(Matrix3x3 other) {
        Matrix3x3 result = new Matrix3x3();
        for(int i=0; i<3; i++) for(int j=0; j<3; j++) result.m[i][j] = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    result.m[i][j] += this.m[i][k] * other.m[k][j];
                }
            }
        }
        return result;
    }

    public Point transform(Point p) {
        double x = m[0][0] * p.x + m[0][1] * p.y + m[0][2];
        double y = m[1][0] * p.x + m[1][1] * p.y + m[1][2];
        return new Point((int) Math.round(x), (int) Math.round(y));
    }
}