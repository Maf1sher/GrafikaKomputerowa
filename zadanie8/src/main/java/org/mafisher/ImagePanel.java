package org.mafisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

class ImagePanel extends JComponent {
    private BufferedImage canvas;
    private double scale = 1.0;
    private Point dragStart;
    private int offsetX = 0, offsetY = 0;
    private Consumer<int[]> pixelInfoConsumer;

    ImagePanel() {
        setBackground(Color.DARK_GRAY);
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    Point p = e.getPoint();
                    offsetX += p.x - dragStart.x;
                    offsetY += p.y - dragStart.y;
                    dragStart = p;
                    repaint();
                    updatePixelInfo(e.getPoint());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updatePixelInfo(e.getPoint());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (canvas == null) return;
                int notches = e.getWheelRotation();
                double oldScale = scale;
                double factor = Math.pow(1.15, -notches);
                scale *= factor;
                if (scale < 0.1) scale = 0.1;
                if (scale > 64) scale = 64;
                Point p = e.getPoint();
                double px = (p.x - offsetX) / oldScale;
                double py = (p.y - offsetY) / oldScale;
                offsetX = (int) Math.round(p.x - px * scale);
                offsetY = (int) Math.round(p.y - py * scale);
                repaint();
                updatePixelInfo(e.getPoint());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    fitToWindow();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    void setImage(BufferedImage img) {
        this.canvas = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        scale = Math.min(1.0, Math.min(getWidth() / (double) Math.max(1, img.getWidth()),
                getHeight() / (double) Math.max(1, img.getHeight())));
        offsetX = (getWidth() - (int)(img.getWidth()*scale))/2;
        offsetY = (getHeight() - (int)(img.getHeight()*scale))/2;
        revalidate();
        repaint();
    }

    BufferedImage getCanvas() {
        return canvas;
    }

    void updateCanvas(BufferedImage newImage) {
        if (canvas != null) {
            Graphics2D g = canvas.createGraphics();
            g.drawImage(newImage, 0, 0, null);
            g.dispose();
            repaint();
        }
    }

    void fitToWindow() {
        if (canvas == null) return;
        scale = Math.min(getWidth() / (double) canvas.getWidth(), getHeight() / (double) canvas.getHeight());
        offsetX = (getWidth() - (int)(canvas.getWidth()*scale))/2;
        offsetY = (getHeight() - (int)(canvas.getHeight()*scale))/2;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (canvas == null) return new Dimension(400,300);
        int w = (int)Math.round(canvas.getWidth() * scale);
        int h = (int)Math.round(canvas.getHeight() * scale);
        return new Dimension(Math.max(w, 200), Math.max(h, 200));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(),getHeight());

        if (canvas == null) {
            g.setColor(Color.WHITE);
            g.drawString("Brak obrazu. Otwórz plik (PPM P3/P6 lub JPEG).", 20, 20);
            g.dispose();
            return;
        }

        if (scale < 2.0) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        } else {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }

        int imgW = canvas.getWidth();
        int imgH = canvas.getHeight();
        int drawW = (int)Math.round(imgW * scale);
        int drawH = (int)Math.round(imgH * scale);

        g.drawImage(canvas, offsetX, offsetY, drawW, drawH, null);

        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(offsetX, offsetY, Math.max(0, drawW-1), Math.max(0, drawH-1));
        g.dispose();
    }

    void setPixelInfoConsumer(Consumer<int[]> consumer) {
        this.pixelInfoConsumer = consumer;
    }

    private void updatePixelInfo(Point mousePoint) {
        if (pixelInfoConsumer == null || canvas == null) return;
        int imgX = (int) ((mousePoint.x - offsetX) / scale);
        int imgY = (int) ((mousePoint.y - offsetY) / scale);

        if (imgX >= 0 && imgY >= 0 && imgX < canvas.getWidth() && imgY < canvas.getHeight()) {
            int rgb = canvas.getRGB(imgX, imgY);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            pixelInfoConsumer.accept(new int[]{r, g, b});
        } else {
            pixelInfoConsumer.accept(new int[]{-1, -1, -1});
        }
    }
}