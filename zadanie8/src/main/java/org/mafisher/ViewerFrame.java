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
    private final JSlider thresholdSlider;
    private final ImageSaver imageSaver;
    private BufferedImage originalImage;
    private File currentFile;

    private final ImageLoader ppmLoader = new PPMImageLoader();
    private final ImageLoader jpegLoader = new JPEGImageLoader();

    public ViewerFrame() {
        super("PPM / JPEG Viewer - Filtry morfologiczne");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        imagePanel = new ImagePanel();
        JScrollPane sp = new JScrollPane(imagePanel);
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
        right.add(Box.createVerticalStrut(15));

        right.add(new JLabel("Próg binaryzacji:"));
        thresholdSlider = new JSlider(0, 255, 128);
        thresholdSlider.setMajorTickSpacing(50);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPaintLabels(true);
        right.add(thresholdSlider);
        right.add(Box.createVerticalStrut(15));

        JButton resetBtn = new JButton("Reset obrazu");
        resetBtn.addActionListener(e -> resetImage());
        right.add(resetBtn);
        right.add(Box.createVerticalStrut(10));

        JLabel morphLabel = new JLabel("Filtry morfologiczne:");
        morphLabel.setFont(morphLabel.getFont().deriveFont(Font.BOLD));
        right.add(morphLabel);
        right.add(Box.createVerticalStrut(5));

        JButton dilateBtn = new JButton("Dylatacja");
        dilateBtn.addActionListener(e -> applyDilate());
        right.add(dilateBtn);
        right.add(Box.createVerticalStrut(5));

        JButton erodeBtn = new JButton("Erozja");
        erodeBtn.addActionListener(e -> applyErode());
        right.add(erodeBtn);
        right.add(Box.createVerticalStrut(5));

        JButton openBtn2 = new JButton("Otwarcie");
        openBtn2.addActionListener(e -> applyOpen());
        right.add(openBtn2);
        right.add(Box.createVerticalStrut(5));

        JButton closeBtn = new JButton("Domknięcie");
        closeBtn.addActionListener(e -> applyClose());
        right.add(closeBtn);
        right.add(Box.createVerticalStrut(5));

        JButton thinBtn = new JButton("Pocienianie (HoM)");
        thinBtn.addActionListener(e -> applyThin());
        right.add(thinBtn);
        right.add(Box.createVerticalStrut(5));

        JButton thickenBtn = new JButton("Pogrubianie (HoM)");
        thickenBtn.addActionListener(e -> applyThicken());
        right.add(thickenBtn);
        right.add(Box.createVerticalStrut(10));

        JButton customSeBtn = new JButton("Własny el. struktur.");
        customSeBtn.addActionListener(e -> defineCustomSE());
        right.add(customSeBtn);
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

        JScrollPane rightScroll = new JScrollPane(right);
        rightScroll.setPreferredSize(new Dimension(250, 0));
        add(rightScroll, BorderLayout.EAST);

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
                JOptionPane.showMessageDialog(this, "Nieobsługiwany format pliku.", "Błąd",
                        JOptionPane.ERROR_MESSAGE);
                setStatus("Błąd: nieobsługiwany format");
                return;
            }
            originalImage = img;
            currentFile = f;
            imagePanel.setImage(img);
            setStatus("Wczytano: " + f.getName() + " (" + img.getWidth() + "x" + img.getHeight() + ")");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd wczytywania: " + ex.getMessage(), "Błąd",
                    JOptionPane.ERROR_MESSAGE);
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
        BufferedImage current = imagePanel.getCanvas();
        if (current == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do zapisania.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File((currentFile!=null ? stripExt(currentFile.getName()) : "image")+".jpg"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File out = chooser.getSelectedFile();
        try {
            imageSaver.saveAsJpeg(current, out, jpegQualitySlider.getValue()/100f);
            setStatus("Zapisano JPEG: " + out.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage(), "Błąd",
                    JOptionPane.ERROR_MESSAGE);
            setStatus("Błąd zapisu: " + ex.getMessage());
        }
    }

    private void resetImage() {
        if (originalImage != null) {
            imagePanel.setImage(originalImage);
            setStatus("Obraz przywrócony do oryginału");
        }
    }

    private int[][] getDefaultSE() {
        return new int[][] {
                {0, 1, 0},
                {1, 1, 1},
                {0, 1, 0}
        };
    }

    private void applyDilate() {
        if (imagePanel.getCanvas() == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MorphologicalFilter filter = new MorphologicalFilter(getDefaultSE());
            BufferedImage result = filter.dilate(imagePanel.getCanvas(), thresholdSlider.getValue());
            imagePanel.updateCanvas(result);
            setStatus("Zastosowano dylatację");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyErode() {
        if (imagePanel.getCanvas() == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MorphologicalFilter filter = new MorphologicalFilter(getDefaultSE());
            BufferedImage result = filter.erode(imagePanel.getCanvas(), thresholdSlider.getValue());
            imagePanel.updateCanvas(result);
            setStatus("Zastosowano erozję");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyOpen() {
        if (imagePanel.getCanvas() == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MorphologicalFilter filter = new MorphologicalFilter(getDefaultSE());
            BufferedImage result = filter.open(imagePanel.getCanvas(), thresholdSlider.getValue());
            imagePanel.updateCanvas(result);
            setStatus("Zastosowano otwarcie");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyClose() {
        if (imagePanel.getCanvas() == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MorphologicalFilter filter = new MorphologicalFilter(getDefaultSE());
            BufferedImage result = filter.close(imagePanel.getCanvas(), thresholdSlider.getValue());
            imagePanel.updateCanvas(result);
            setStatus("Zastosowano domknięcie");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyThin() {
        if (imagePanel.getCanvas() == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MorphologicalFilter filter = new MorphologicalFilter(getDefaultSE());
            BufferedImage result = filter.thin(imagePanel.getCanvas(), thresholdSlider.getValue());
            imagePanel.updateCanvas(result);
            setStatus("Zastosowano pocienianie (Hit-or-Miss)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyThicken() {
        if (imagePanel.getCanvas() == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MorphologicalFilter filter = new MorphologicalFilter(getDefaultSE());
            BufferedImage result = filter.thicken(imagePanel.getCanvas(), thresholdSlider.getValue());
            imagePanel.updateCanvas(result);
            setStatus("Zastosowano pogrubianie (Hit-or-Miss)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void defineCustomSE() {
        String sizeStr = JOptionPane.showInputDialog(this, "Podaj rozmiar elementu strukturyzującego (np. 3 dla 3x3):", "3");
        if (sizeStr == null) return;

        try {
            int size = Integer.parseInt(sizeStr.trim());
            if (size < 1 || size > 15) {
                JOptionPane.showMessageDialog(this, "Rozmiar musi być między 1 a 15", "Błąd",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            SEEditorDialog dialog = new SEEditorDialog(this, size);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                int[][] se = dialog.getStructuringElement();
                String filterType = dialog.getFilterType();

                MorphologicalFilter filter = new MorphologicalFilter(se);
                BufferedImage result = null;

                switch (filterType) {
                    case "Dylatacja":
                        result = filter.dilate(imagePanel.getCanvas(), thresholdSlider.getValue());
                        break;
                    case "Erozja":
                        result = filter.erode(imagePanel.getCanvas(), thresholdSlider.getValue());
                        break;
                    case "Otwarcie":
                        result = filter.open(imagePanel.getCanvas(), thresholdSlider.getValue());
                        break;
                    case "Domknięcie":
                        result = filter.close(imagePanel.getCanvas(), thresholdSlider.getValue());
                        break;
                }

                if (result != null) {
                    imagePanel.updateCanvas(result);
                    setStatus("Zastosowano " + filterType + " z własnym elementem strukturyzującym");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nieprawidłowy rozmiar", "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i>0 ? name.substring(0,i) : name;
    }
}