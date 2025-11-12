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
    private final HistogramPanel histogramPanel;
    private final ImageSaver imageSaver;
    private BufferedImage currentImage;
    private BufferedImage originalImage;
    private File currentFile;

    private final ImageLoader ppmLoader = new PPMImageLoader();
    private final ImageLoader jpegLoader = new JPEGImageLoader();

    public ViewerFrame() {
        super("PPM / JPEG Viewer - Histogram i Binaryzacja");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 840);
        setLocationRelativeTo(null);

        imagePanel = new ImagePanel();
        JScrollPane sp = new JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        right.setPreferredSize(new Dimension(300, 0));

        JButton openBtn = new JButton("Otwórz obraz...");
        openBtn.addActionListener(e -> openImage());
        right.add(openBtn);
        right.add(Box.createVerticalStrut(5));

        JButton restoreBtn = new JButton("Przywróć oryginał");
        restoreBtn.addActionListener(e -> restoreOriginal());
        right.add(restoreBtn);
        right.add(Box.createVerticalStrut(5));

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

        JSeparator sep1 = new JSeparator();
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        right.add(sep1);
        right.add(Box.createVerticalStrut(10));

        histogramPanel = new HistogramPanel();
        histogramPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        histogramPanel.setMinimumSize(new Dimension(280, 220));
        histogramPanel.setPreferredSize(new Dimension(280, 220));
        histogramPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        right.add(histogramPanel);
        right.add(Box.createVerticalStrut(10));

        JSeparator sep1b = new JSeparator();
        sep1b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        right.add(sep1b);
        right.add(Box.createVerticalStrut(10));

        JLabel histLabel = new JLabel("Operacje histogramu:");
        histLabel.setFont(histLabel.getFont().deriveFont(Font.BOLD));
        right.add(histLabel);
        right.add(Box.createVerticalStrut(5));

        JButton stretchBtn = new JButton("Rozszerzenie histogramu");
        stretchBtn.addActionListener(e -> applyHistogramStretch());
        right.add(stretchBtn);
        right.add(Box.createVerticalStrut(5));

        JButton equalizeBtn = new JButton("Wyrównanie histogramu");
        equalizeBtn.addActionListener(e -> applyHistogramEqualization());
        right.add(equalizeBtn);
        right.add(Box.createVerticalStrut(10));

        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        right.add(sep2);
        right.add(Box.createVerticalStrut(10));

        JLabel binLabel = new JLabel("Binaryzacja:");
        binLabel.setFont(binLabel.getFont().deriveFont(Font.BOLD));
        right.add(binLabel);
        right.add(Box.createVerticalStrut(5));

        JButton manualBinBtn = new JButton("Ręczna (podaj próg)");
        manualBinBtn.addActionListener(e -> applyManualBinarization());
        right.add(manualBinBtn);
        right.add(Box.createVerticalStrut(5));

        JButton percentBlackBtn = new JButton("Procentowa selekcja czarnego");
        percentBlackBtn.addActionListener(e -> applyPercentBlackBinarization());
        right.add(percentBlackBtn);
        right.add(Box.createVerticalStrut(5));

        JButton meanIterBtn = new JButton("Selekcja iteratywna średniej");
        meanIterBtn.addActionListener(e -> applyMeanIterativeBinarization());
        right.add(meanIterBtn);
        right.add(Box.createVerticalStrut(10));

        pixelInfoLabel = new JLabel("R: ---, G: ---, B: ---");
        pixelInfoLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        pixelInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pixelInfoLabel.setBorder(BorderFactory.createTitledBorder("Kolor pod kursorem"));
        pixelInfoLabel.setMinimumSize(new Dimension(280, 50));
        pixelInfoLabel.setPreferredSize(new Dimension(280, 50));
        pixelInfoLabel.setMaximumSize(new Dimension(280, 50));
        right.add(pixelInfoLabel);
        right.add(Box.createVerticalStrut(10));

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
        pixelInfoLabel.setText(r < 0 ? "R: ---, G: ---, B: ---" : String.format("R: %3d, G: %3d, B: %3d", r, g, b));
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
            originalImage = copyImage(img);
            imagePanel.setImage(img);
            histogramPanel.updateHistogram(img);
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

    private void applyHistogramStretch() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            setStatus("Rozszerzanie histogramu...");
            BufferedImage result = HistogramOperations.stretchHistogram(currentImage);
            currentImage = result;
            imagePanel.setImage(result);
            histogramPanel.updateHistogram(result);
            setStatus("Zastosowano rozszerzenie histogramu");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            setStatus("Błąd operacji");
        }
    }

    private void applyHistogramEqualization() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            setStatus("Wyrównywanie histogramu...");
            BufferedImage result = HistogramOperations.equalizeHistogram(currentImage);
            currentImage = result;
            imagePanel.setImage(result);
            histogramPanel.updateHistogram(result);
            setStatus("Zastosowano wyrównanie histogramu");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            setStatus("Błąd operacji");
        }
    }

    private void applyManualBinarization() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Podaj próg binaryzacji (0-255):", "128");
        if (input == null) return;
        try {
            int threshold = Integer.parseInt(input.trim());
            if (threshold < 0 || threshold > 255) {
                JOptionPane.showMessageDialog(this, "Próg musi być w zakresie 0-255.", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }
            setStatus("Binaryzacja ręczna (próg: " + threshold + ")...");
            BufferedImage result = BinarizationOperations.binarizeManual(currentImage, threshold);
            currentImage = result;
            imagePanel.setImage(result);
            histogramPanel.updateHistogram(result);
            setStatus("Zastosowano binaryzację ręczną (próg: " + threshold + ")");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nieprawidłowa wartość progu.", "Błąd", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            setStatus("Błąd operacji");
        }
    }

    private void applyPercentBlackBinarization() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Podaj procent czarnych pikseli (0-100):", "50");
        if (input == null) return;
        try {
            double percent = Double.parseDouble(input.trim());
            if (percent < 0 || percent > 100) {
                JOptionPane.showMessageDialog(this, "Procent musi być w zakresie 0-100.", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }
            setStatus("Binaryzacja procentowa (" + percent + "%)...");
            BufferedImage result = BinarizationOperations.binarizePercentBlack(currentImage, percent);
            currentImage = result;
            imagePanel.setImage(result);
            histogramPanel.updateHistogram(result);
            setStatus("Zastosowano binaryzację procentową (" + percent + "%)");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nieprawidłowa wartość procentu.", "Błąd", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            setStatus("Błąd operacji");
        }
    }

    private void applyMeanIterativeBinarization() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            setStatus("Binaryzacja iteratywna średniej...");
            BufferedImage result = BinarizationOperations.binarizeMeanIterative(currentImage);
            currentImage = result;
            imagePanel.setImage(result);
            histogramPanel.updateHistogram(result);
            setStatus("Zastosowano binaryzację iteratywną średniej");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            setStatus("Błąd operacji");
        }
    }

    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i>0 ? name.substring(0,i) : name;
    }

    private void restoreOriginal() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Brak oryginalnego obrazu.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentImage = copyImage(originalImage);
        imagePanel.setImage(currentImage);
        histogramPanel.updateHistogram(currentImage);
        setStatus("Przywrócono oryginalny obraz");
    }

    private BufferedImage copyImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }
}