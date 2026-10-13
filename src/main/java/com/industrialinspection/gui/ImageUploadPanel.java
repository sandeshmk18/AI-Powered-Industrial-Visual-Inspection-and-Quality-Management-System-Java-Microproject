package com.industrialinspection.gui;

import com.industrialinspection.model.AIPrediction;
import com.industrialinspection.model.Product;
import com.industrialinspection.model.ProductImage;
import com.industrialinspection.model.ProductionBatch;
import com.industrialinspection.model.User;
import com.industrialinspection.service.AIPredictionService;
import com.industrialinspection.service.BatchService;
import com.industrialinspection.service.ImageService;
import com.industrialinspection.service.ProductService;
import com.industrialinspection.util.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * 6. Optical Inspection Image Capture & Upload Panel.
 */
public class ImageUploadPanel extends JPanel {

    private final ImageService imageService = new ImageService();
    private final ProductService productService = new ProductService();
    private final BatchService batchService = new BatchService();
    private final AIPredictionService aiPredictionService = new AIPredictionService();
    private final User currentUser;

    private File selectedFile;
    private final JLabel previewLabel;
    private final JLabel infoLabel;
    private final JComboBox<Product> productCb;
    private final JComboBox<ProductionBatch> batchCb;
    private final JTextField cameraField;
    private final JButton uploadBtn;

    public ImageUploadPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARKEST);
        JLabel title = new JLabel("Optical Inspection Image Acquisition");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JLabel sub = new JLabel("Supports High-Resolution Captures (JPG, JPEG, PNG, BMP up to 25MB)");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Center split: Form on left, Preview on right
        JPanel split = new JPanel(new GridLayout(1, 2, 20, 20));
        split.setBackground(UITheme.BG_DARKEST);

        // Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        List<Product> products = productService.getAllProducts();
        productCb = new JComboBox<>(products.toArray(new Product[0]));
        productCb.setMaximumSize(new Dimension(500, 32));

        List<ProductionBatch> batches = batchService.getAllBatches();
        batchCb = new JComboBox<>(batches.toArray(new ProductionBatch[0]));
        batchCb.setMaximumSize(new Dimension(500, 32));

        cameraField = UITheme.createTextField();
        cameraField.setText("CAM-OPTICAL-01");
        cameraField.setMaximumSize(new Dimension(500, 32));

        JButton chooseBtn = UITheme.createPrimaryButton("Browse Image File (JFileChooser)...");
        chooseBtn.setMaximumSize(new Dimension(500, 36));
        chooseBtn.addActionListener(e -> chooseImageFile());

        JButton genSampleBtn = UITheme.createSecondaryButton("Generate Synthetic Component Image");
        genSampleBtn.setMaximumSize(new Dimension(500, 32));
        genSampleBtn.addActionListener(e -> generateSyntheticSample());

        uploadBtn = UITheme.createSuccessButton("Save & Register Image");
        uploadBtn.setMaximumSize(new Dimension(500, 40));
        uploadBtn.setEnabled(false);
        uploadBtn.addActionListener(e -> uploadImage());

        infoLabel = new JLabel("No image selected");
        infoLabel.setFont(UITheme.FONT_SMALL);
        infoLabel.setForeground(UITheme.ACCENT_CYAN);

        formCard.add(createFieldHeader("1. Target Product Component:"));
        formCard.add(productCb);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));

        formCard.add(createFieldHeader("2. Associated Production Lot / Batch:"));
        formCard.add(batchCb);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));

        formCard.add(createFieldHeader("3. Camera Mount Identifier:"));
        formCard.add(cameraField);
        formCard.add(Box.createRigidArea(new Dimension(0, 16)));

        formCard.add(chooseBtn);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));
        formCard.add(genSampleBtn);
        formCard.add(Box.createRigidArea(new Dimension(0, 14)));

        formCard.add(infoLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 14)));
        formCard.add(uploadBtn);

        split.add(formCard);

        // Preview Card
        JPanel previewCard = UITheme.createCardPanel();
        previewCard.setLayout(new BorderLayout());

        JLabel pTitle = new JLabel("High-Resolution Inspection Preview");
        pTitle.setFont(UITheme.FONT_HEADER_MED);
        pTitle.setForeground(UITheme.TEXT_MAIN);
        pTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        previewCard.add(pTitle, BorderLayout.NORTH);

        previewLabel = new JLabel("Preview will appear here", SwingConstants.CENTER);
        previewLabel.setFont(UITheme.FONT_REGULAR);
        previewLabel.setForeground(UITheme.TEXT_MUTED);
        previewLabel.setBackground(new Color(20, 24, 32));
        previewLabel.setOpaque(true);
        previewLabel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        previewCard.add(previewLabel, BorderLayout.CENTER);
        split.add(previewCard);

        add(split, BorderLayout.CENTER);
    }

    private JLabel createFieldHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void chooseImageFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Industrial Inspection Image");
        fc.setFileFilter(new FileNameExtensionFilter("Optical Inspection Images (JPG, PNG, BMP)", "jpg", "jpeg", "png", "bmp"));
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            loadFile(fc.getSelectedFile());
        }
    }

    private void generateSyntheticSample() {
        Product p = (Product) productCb.getSelectedItem();
        String name = (p != null) ? p.getProductName() : "Brake_Rotor_320mm";
        String cleanName = name.replaceAll("[^a-zA-Z0-9]", "_");
        File sample = ImageUtil.createSampleIndustrialImage("sim_" + cleanName + "_" + System.currentTimeMillis() % 1000 + ".png", name, true);
        if (sample != null && sample.exists()) {
            loadFile(sample);
            JOptionPane.showMessageDialog(this, "Generated sample optical inspection file for testing: " + sample.getName());
        }
    }

    private void loadFile(File file) {
        this.selectedFile = file;
        try {
            BufferedImage bimg = ImageIO.read(file);
            if (bimg == null) {
                JOptionPane.showMessageDialog(this, "Failed to read image format.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int w = bimg.getWidth();
            int h = bimg.getHeight();
            long size = file.length();
            String sizeStr = (size / 1024) + " KB";

            infoLabel.setText(String.format("File: %s | Dimensions: %d x %d px | Size: %s", file.getName(), w, h, sizeStr));
            uploadBtn.setEnabled(true);

            // Thumbnail
            BufferedImage thumb = ImageUtil.createThumbnail(bimg, 440, 360);
            previewLabel.setIcon(new ImageIcon(thumb));
            previewLabel.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Image load error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void uploadImage() {
        if (selectedFile == null) return;
        Product p = (Product) productCb.getSelectedItem();
        ProductionBatch b = (ProductionBatch) batchCb.getSelectedItem();
        if (p == null || b == null) {
            JOptionPane.showMessageDialog(this, "Please select both a Product and a Batch.");
            return;
        }

        ProductImage img;
        try {
            img = imageService.uploadAndRegisterImage(
                    selectedFile, p.getProductId(), b.getBatchId(),
                    cameraField.getText().trim(), currentUser != null ? currentUser.getUserId() : null);

            uploadBtn.setEnabled(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Upload failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer userId = currentUser != null ? currentUser.getUserId() : null;
            AIPrediction prediction = aiPredictionService.runPrediction(img, userId);
            boolean isPass = prediction.isDefectFree() || "DEFECT_FREE".equalsIgnoreCase(prediction.getPredictedLabel());
            String status = isPass ? "PASS" : "FAIL";

            String resultMessage = String.format(
                    "Image successfully uploaded and registered in inspection database!\n" +
                    "Assigned Image ID: #%d\n" +
                    "File: %s\n\n" +
                    "--- Simulated Inspection Result: %s ---\n" +
                    "(Simulation Notice: Generated by SimulatedAIInspectionService heuristic engine, not a trained AI model)\n\n" +
                    "Status: %s\n" +
                    "Prediction Label: %s\n" +
                    "Confidence: %.2f%%\n" +
                    "Severity: %s\n" +
                    "Defect Location: %s\n" +
                    "Processing Time: %d ms",
                    img.getImageId(),
                    img.getFileName(),
                    status,
                    status,
                    prediction.getPredictedLabel(),
                    prediction.getConfidenceScore(),
                    prediction.getSeverity(),
                    prediction.getDefectLocation(),
                    prediction.getProcessingTimeMs()
            );

            JOptionPane.showMessageDialog(this, resultMessage,
                    "Upload & Simulated Inspection - " + status,
                    isPass ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Image successfully uploaded and registered in database!\n" +
                    "Assigned Image ID: #" + img.getImageId() + "\n" +
                    "File: " + img.getFileName() + "\n\n" +
                    "However, simulated AI visual inspection failed: " + ex.getMessage(),
                    "Inspection Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}
