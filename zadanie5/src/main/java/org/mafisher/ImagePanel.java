package org.mafisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

class ImagePanel extends JComponent {
    private BufferedImage image;
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
                if (image == null) return;
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
        this.image = img;
        scale = Math.min(1.0, Math.min(getWidth() / (double) Math.max(1,img.getWidth()), getHeight() / (double) Math.max(1,img.getHeight())));
        offsetX = (getWidth() - (int)(img.getWidth()*scale))/2;
        offsetY = (getHeight() - (int)(img.getHeight()*scale))/2;
        revalidate();
        repaint();
    }

    void fitToWindow() {
        if (image == null) return;
        scale = Math.min(getWidth() / (double) image.getWidth(), getHeight() / (double) image.getHeight());
        offsetX = (getWidth() - (int)(image.getWidth()*scale))/2;
        offsetY = (getHeight() - (int)(image.getHeight()*scale))/2;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (image == null) return new Dimension(400,300);
        int w = (int)Math.round(image.getWidth() * scale);
        int h = (int)Math.round(image.getHeight() * scale);
        return new Dimension(Math.max(w, 200), Math.max(h, 200));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(),getHeight());

        if (image == null) {
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

        int imgW = image.getWidth();
        int imgH = image.getHeight();
        int drawW = (int)Math.round(imgW * scale);
        int drawH = (int)Math.round(imgH * scale);

        g.drawImage(image, offsetX, offsetY, drawW, drawH, null);

        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(offsetX, offsetY, Math.max(0, drawW-1), Math.max(0, drawH-1));
        g.dispose();
    }

    void setPixelInfoConsumer(Consumer<int[]> consumer) {
        this.pixelInfoConsumer = consumer;
    }

    private void updatePixelInfo(Point mousePoint) {
        if (pixelInfoConsumer == null || image == null) return;
        int imgX = (int) ((mousePoint.x - offsetX) / scale);
        int imgY = (int) ((mousePoint.y - offsetY) / scale);

        if (imgX >= 0 && imgY >= 0 && imgX < image.getWidth() && imgY < image.getHeight()) {
            int rgb = image.getRGB(imgX, imgY);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            pixelInfoConsumer.accept(new int[]{r, g, b});
        } else {
            pixelInfoConsumer.accept(new int[]{-1, -1, -1});
        }
    }
}
