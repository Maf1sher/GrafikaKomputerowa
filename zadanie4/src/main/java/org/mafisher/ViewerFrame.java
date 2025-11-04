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
    private final ImageProcessor imageProcessor;
    private BufferedImage currentImage;
    private BufferedImage originalImage;
    private File currentFile;

    private final ImageLoader ppmLoader = new PPMImageLoader();
    private final ImageLoader jpegLoader = new JPEGImageLoader();

    public ViewerFrame() {
        super("PPM / JPEG Viewer - Przetwarzanie obrazów");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
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
        right.add(Box.createVerticalStrut(5));

        JButton saveJpegBtn = new JButton("Zapisz jako JPEG...");
        saveJpegBtn.addActionListener(e -> saveAsJpeg());
        right.add(saveJpegBtn);
        right.add(Box.createVerticalStrut(5));

        JButton resetBtn = new JButton("Resetuj do oryginału");
        resetBtn.addActionListener(e -> resetToOriginal());
        right.add(resetBtn);
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

        right.add(createSeparator("Przekształcenia punktowe"));

        JButton addBtn = new JButton("Dodawanie");
        addBtn.addActionListener(e -> applyPointTransform("add"));
        right.add(addBtn);
        right.add(Box.createVerticalStrut(5));

        JButton subtractBtn = new JButton("Odejmowanie");
        subtractBtn.addActionListener(e -> applyPointTransform("subtract"));
        right.add(subtractBtn);
        right.add(Box.createVerticalStrut(5));

        JButton multiplyBtn = new JButton("Mnożenie");
        multiplyBtn.addActionListener(e -> applyPointTransform("multiply"));
        right.add(multiplyBtn);
        right.add(Box.createVerticalStrut(5));

        JButton divideBtn = new JButton("Dzielenie");
        divideBtn.addActionListener(e -> applyPointTransform("divide"));
        right.add(divideBtn);
        right.add(Box.createVerticalStrut(5));

        JButton brightnessBtn = new JButton("Zmiana jasności");
        brightnessBtn.addActionListener(e -> applyBrightness());
        right.add(brightnessBtn);
        right.add(Box.createVerticalStrut(5));

        JButton grayAvgBtn = new JButton("Skala szarości (średnia)");
        grayAvgBtn.addActionListener(e -> applyFilter("gray_avg"));
        right.add(grayAvgBtn);
        right.add(Box.createVerticalStrut(5));

        JButton grayLumBtn = new JButton("Skala szarości (luminancja)");
        grayLumBtn.addActionListener(e -> applyFilter("gray_lum"));
        right.add(grayLumBtn);
        right.add(Box.createVerticalStrut(10));

        right.add(createSeparator("Filtry"));

        JButton smoothBtn = new JButton("Wygładzający");
        smoothBtn.addActionListener(e -> applyFilter("smooth"));
        right.add(smoothBtn);
        right.add(Box.createVerticalStrut(5));

        JButton medianBtn = new JButton("Medianowy");
        medianBtn.addActionListener(e -> applyFilter("median"));
        right.add(medianBtn);
        right.add(Box.createVerticalStrut(5));

        JButton sobelBtn = new JButton("Sobel (krawędzie)");
        sobelBtn.addActionListener(e -> applyFilter("sobel"));
        right.add(sobelBtn);
        right.add(Box.createVerticalStrut(5));

        JButton sharpenBtn = new JButton("Wyostrzający");
        sharpenBtn.addActionListener(e -> applyFilter("sharpen"));
        right.add(sharpenBtn);
        right.add(Box.createVerticalStrut(5));

        JButton gaussBtn = new JButton("Rozmycie Gaussa");
        gaussBtn.addActionListener(e -> applyFilter("gauss"));
        right.add(gaussBtn);
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
        imageProcessor = new ImageProcessor();
    }

    private JPanel createSeparator(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JSeparator(), BorderLayout.CENTER);
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, BorderLayout.NORTH);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return panel;
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
            originalImage = copyImage(img);
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

    private void resetToOriginal() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Brak oryginalnego obrazu.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentImage = copyImage(originalImage);
        imagePanel.setImage(currentImage);
        setStatus("Przywrócono oryginalny obraz");
    }

    private void applyPointTransform(String type) {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField rField = new JTextField("0");
        JTextField gField = new JTextField("0");
        JTextField bField = new JTextField("0");
        panel.add(new JLabel("R:"));
        panel.add(rField);
        panel.add(new JLabel("G:"));
        panel.add(gField);
        panel.add(new JLabel("B:"));
        panel.add(bField);

        String title = "";
        switch(type) {
            case "add": title = "Dodawanie"; break;
            case "subtract": title = "Odejmowanie"; break;
            case "multiply": title = "Mnożenie"; break;
            case "divide": title = "Dzielenie"; break;
        }

        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            BufferedImage processed = null;
            switch(type) {
                case "add":
                    int ar = Integer.parseInt(rField.getText());
                    int ag = Integer.parseInt(gField.getText());
                    int ab = Integer.parseInt(bField.getText());
                    processed = imageProcessor.add(currentImage, ar, ag, ab);
                    break;
                case "subtract":
                    int sr = Integer.parseInt(rField.getText());
                    int sg = Integer.parseInt(gField.getText());
                    int sb = Integer.parseInt(bField.getText());
                    processed = imageProcessor.subtract(currentImage, sr, sg, sb);
                    break;
                case "multiply":
                    double mr = Double.parseDouble(rField.getText());
                    double mg = Double.parseDouble(gField.getText());
                    double mb = Double.parseDouble(bField.getText());
                    processed = imageProcessor.multiply(currentImage, mr, mg, mb);
                    break;
                case "divide":
                    double dr = Double.parseDouble(rField.getText());
                    double dg = Double.parseDouble(gField.getText());
                    double db = Double.parseDouble(bField.getText());
                    processed = imageProcessor.divide(currentImage, dr, dg, db);
                    break;
            }

            if (processed != null) {
                currentImage = processed;
                imagePanel.setImage(currentImage);
                setStatus("Zastosowano: " + title);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nieprawidłowe wartości numeryczne.", "Błąd", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyBrightness() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Zmiana jasności (-255 do 255):", "0");
        if (input == null) return;

        try {
            int delta = Integer.parseInt(input);
            BufferedImage processed = imageProcessor.changeBrightness(currentImage, delta);
            currentImage = processed;
            imagePanel.setImage(currentImage);
            setStatus("Zastosowano zmianę jasności: " + delta);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nieprawidłowa wartość numeryczna.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter(String filterType) {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Brak obrazu do przetworzenia.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setStatus("Przetwarzanie...");
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                return switch (filterType) {
                    case "smooth" -> imageProcessor.smoothingFilter(currentImage);
                    case "median" -> imageProcessor.medianFilter(currentImage);
                    case "sobel" -> imageProcessor.sobelFilter(currentImage);
                    case "sharpen" -> imageProcessor.sharpenFilter(currentImage);
                    case "gauss" -> imageProcessor.gaussianBlur(currentImage);
                    case "gray_avg" -> imageProcessor.toGrayscaleAverage(currentImage);
                    case "gray_lum" -> imageProcessor.toGrayscaleLuminosity(currentImage);
                    default -> null;
                };
            }

            @Override
            protected void done() {
                try {
                    BufferedImage result = get();
                    if (result != null) {
                        currentImage = result;
                        imagePanel.setImage(currentImage);
                        setStatus("Zastosowano filtr: " + filterType);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ViewerFrame.this, "Błąd przetwarzania: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                    setStatus("Błąd przetwarzania");
                }
            }
        };
        worker.execute();
    }


    private BufferedImage copyImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i>0 ? name.substring(0,i) : name;
    }
}