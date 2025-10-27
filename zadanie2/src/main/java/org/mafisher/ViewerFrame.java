package org.mafisher;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ViewerFrame extends JFrame {

    private final ImagePanel imagePanel;
    private final JLabel statusLabel;
    private final JLabel pixelInfoLabel;
    private final JSlider jpegQualitySlider;
    private final JCheckBox linearScaleCheckbox;
    private final ImageSaver imageSaver;
    private BufferedImage currentImage;
    private File currentFile;

    private final ImageLoader ppmLoader = new PPMImageLoader();
    private final ImageLoader jpegLoader = new JPEGImageLoader();

    public ViewerFrame() {
        super("PPM / JPEG Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        imagePanel = new ImagePanel();
        JScrollPane sp = new JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton openBtn = new JButton("Otwórz obraz...");
        openBtn.addActionListener(e -> openImage());
        right.add(openBtn);
        right.add(Box.createVerticalStrut(10));

        JButton saveJpegBtn = new JButton("Zapisz jako JPEG...");
        saveJpegBtn.addActionListener(e -> saveAsJpeg());
        right.add(saveJpegBtn);
        right.add(Box.createVerticalStrut(10));

        right.add(new JLabel("Jakość JPEG:"));
        jpegQualitySlider = new JSlider(1, 100, 90);
        jpegQualitySlider.setMajorTickSpacing(10);
        jpegQualitySlider.setPaintTicks(true);
        jpegQualitySlider.setPaintLabels(true);
        right.add(jpegQualitySlider);
        right.add(Box.createVerticalStrut(10));

        linearScaleCheckbox = new JCheckBox("Liniowe skalowanie kolorów", true);
        right.add(linearScaleCheckbox);
        right.add(Box.createVerticalStrut(10));

        pixelInfoLabel = new JLabel("R: 255, G: 255, B: 255");
        pixelInfoLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        pixelInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pixelInfoLabel.setBorder(BorderFactory.createTitledBorder("Kolor pod kursorem"));
        pixelInfoLabel.setMinimumSize(new Dimension(200,40));
        pixelInfoLabel.setPreferredSize(new Dimension(200,40));
        pixelInfoLabel.setMaximumSize(new Dimension(200,40));
        right.add(pixelInfoLabel);
        right.add(Box.createVerticalGlue());

        JButton fitBtn = new JButton("Dopasuj do okna");
        fitBtn.addActionListener(e -> imagePanel.fitToWindow());
        right.add(fitBtn);

        add(right, BorderLayout.EAST);

        statusLabel = new JLabel("Gotowy.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        add(statusLabel, BorderLayout.SOUTH);

        imagePanel.setPixelInfoConsumer(rgb -> updatePixelInfo(rgb[0], rgb[1], rgb[2]));

        imageSaver = new ImageSaver();
    }

    private void setStatus(String s) {
        statusLabel.setText(s);
    }

    private void updatePixelInfo(int r, int g, int b) {
        pixelInfoLabel.setText(r<0 ? "R: -, G: -, B: -" : String.format("R: %d, G: %d, B: %d", r, g, b));
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try {
            setStatus("Wczytywanie " + f.getName());
            BufferedImage img = loadFile(f);
            if (img == null) {
                JOptionPane.showMessageDialog(this, "Nieobsługiwany format pliku.", "Błąd", JOptionPane.ERROR_MESSAGE);
                setStatus("Błąd: nieobsługiwany format");
                return;
            }
            currentImage = img;
            currentFile = f;
            imagePanel.setImage(img);
            setStatus("Wczytano: " + f.getName() + " (" + img.getWidth() + "x" + img.getHeight() + ")");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd wczytywania: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            setStatus("Błąd: " + ex.getMessage());
        }
    }

    private BufferedImage loadFile(File file) throws Exception {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".ppm")) return ppmLoader.load(file, linearScaleCheckbox.isSelected());
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return jpegLoader.load(file, true);
        return null;
    }

    private void saveAsJpeg() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do zapisania.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File((currentFile!=null ? stripExt(currentFile.getName()) : "image")+".jpg"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File out = chooser.getSelectedFile();
        try {
            imageSaver.saveAsJpeg(currentImage, out, jpegQualitySlider.getValue()/100f);
            setStatus("Zapisano JPEG: " + out.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            setStatus("Błąd zapisu: " + ex.getMessage());
        }
    }

    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i>0 ? name.substring(0,i) : name;
    }
}
