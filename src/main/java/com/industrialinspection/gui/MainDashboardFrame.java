package com.industrialinspection.gui;

import com.industrialinspection.dao.DBConnection;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.UserRole;
import com.industrialinspection.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * 3. Central Application Shell & Industrial Navigation Dashboard Frame.
 * Features ultra-visible, high-contrast sidebar tabs, vibrant status badges, and responsive workspace.
 */
public class MainDashboardFrame extends JFrame {

    private final User currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final Map<String, NavTabButton> navButtons = new HashMap<>();

    public MainDashboardFrame(User user) {
        this.currentUser = (user != null) ? user : new User(1, "admin", "Admin User", "admin@abc.com", UserRole.ADMINISTRATOR);

        setTitle("ABC Precision Manufacturing - Visual Inspection & Quality Management System");
        setSize(1380, 860);
        setMinimumSize(new Dimension(1120, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARKEST);

        // Top Navigation Header
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // Center Split: Left Sidebar + Central Card Workspace
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(UITheme.BG_DARKEST);

        workspace.add(createSidebarPanel(), BorderLayout.WEST);

        initCards();
        workspace.add(contentCards, BorderLayout.CENTER);

        root.add(workspace, BorderLayout.CENTER);
        setContentPane(root);

        // Default active view
        switchCard("QUALITY");
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(UITheme.BG_SIDEBAR);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Brand & Facility
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setBackground(UITheme.BG_SIDEBAR);

        JLabel logoBadge = new JLabel("ABC");
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setBackground(UITheme.ACCENT_BLUE);
        logoBadge.setOpaque(true);
        logoBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel title = new JLabel("ABC PRECISION MANUFACTURING PVT. LTD.");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.TEXT_MAIN);

        JLabel facility = new JLabel("|  Plant 3 Aerospace & Automotive Metrology Cell");
        facility.setFont(UITheme.FONT_SMALL);
        facility.setForeground(UITheme.TEXT_MUTED);

        brand.add(logoBadge);
        brand.add(title);
        brand.add(facility);
        header.add(brand, BorderLayout.WEST);

        // User Profile & Status Banner
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setBackground(UITheme.BG_SIDEBAR);

        boolean dbLive = DBConnection.getInstance().testConnection();
        JLabel dbStatus = new JLabel(dbLive ? "● [ONLINE] MySQL Live" : "● [OFFLINE] In-Memory Cache");
        dbStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        dbStatus.setForeground(dbLive ? UITheme.ACCENT_GREEN : UITheme.ACCENT_AMBER);
        dbStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(dbLive ? new Color(16, 185, 129, 140) : new Color(251, 146, 60, 140), 1),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JPanel userPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        userPill.setBackground(UITheme.BG_CARD);
        userPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel userBadge = new JLabel(currentUser.getFullName() + " (" + currentUser.getRole().getDisplayName() + ")");
        userBadge.setFont(UITheme.FONT_BOLD);
        userBadge.setForeground(UITheme.ACCENT_CYAN);
        userPill.add(userBadge);

        JButton logoutBtn = UITheme.createSecondaryButton("Sign Out");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        logoutBtn.addActionListener(e -> {
            new AuthenticationService().logout(currentUser, "127.0.0.1");
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        right.add(dbStatus);
        right.add(userPill);
        right.add(logoutBtn);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR),
                new EmptyBorder(14, 12, 14, 12)
        ));

        addNavButton(sidebar, "QUALITY", "📊", "Dashboard");
        addNavButton(sidebar, "INSPECTIONS", "🔍", "Inspections");
        addNavButton(sidebar, "IMAGES", "📷", "Upload Image");
        addNavButton(sidebar, "PRODUCTS", "📦", "Products");
        addNavButton(sidebar, "REPORTS", "📈", "Reports");

        sidebar.add(Box.createVerticalGlue());

        NavTabButton aboutBtn = new NavTabButton("ℹ️", "About System");
        aboutBtn.addActionListener(e -> new AboutSystemDialog(this).setVisible(true));
        sidebar.add(aboutBtn);

        return sidebar;
    }

    private void addNavButton(JPanel container, String cardName, String icon, String label) {
        NavTabButton btn = new NavTabButton(icon, label);
        btn.addActionListener(e -> switchCard(cardName));
        navButtons.put(cardName, btn);
        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private JPanel createSectionHeader(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(230, 24));

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 8));
        dot.setForeground(UITheme.ACCENT_CYAN);

        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(0, 210, 255));

        p.add(dot);
        p.add(l);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private void initCards() {
        contentCards.setBackground(UITheme.BG_DARKEST);
        contentCards.add(new QualityDashboardPanel(currentUser), "QUALITY");
        contentCards.add(new InspectionDashboardPanel(currentUser), "INSPECTIONS");
        contentCards.add(new ImageUploadPanel(currentUser), "IMAGES");
        contentCards.add(new CorrectiveActionPanel(currentUser), "CAPA");
        contentCards.add(new DefectClassificationPanel(currentUser), "DEFECTS");
        contentCards.add(new ProductManagementPanel(currentUser), "PRODUCTS");
        contentCards.add(new BatchManagementPanel(currentUser), "BATCHES");
        contentCards.add(new MachineManagementPanel(currentUser), "MACHINES");
        contentCards.add(new OperatorManagementPanel(currentUser), "OPERATORS");
        contentCards.add(new ReportsPanel(currentUser), "REPORTS");
        contentCards.add(new UserManagementPanel(currentUser), "USERS");
        contentCards.add(new SettingsPanel(currentUser), "SETTINGS");
        contentCards.add(new DatabaseBackupPanel(currentUser), "BACKUP");
    }

    public void switchCard(String name) {
        cardLayout.show(contentCards, name);
        for (Map.Entry<String, NavTabButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(name);
            entry.getValue().setActive(active);
        }
    }

    /**
     * Highly Visible, High-Contrast Sidebar Navigation Tab Button.
     * Guaranteed to be clearly delineated, with custom icons, hover state, and neon active indicator.
     */
    public static class NavTabButton extends JButton {
        private boolean active = false;
        private final String iconSymbol;
        private final String labelText;

        public NavTabButton(String iconSymbol, String labelText) {
            super();
            this.iconSymbol = iconSymbol;
            this.labelText = labelText;
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(220, 38));
            setMaximumSize(new Dimension(230, 38));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            ButtonModel model = getModel();

            if (active) {
                // Active / selected tab: illuminated rich blue gradient + cyan pill border
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(24, 60, 115),
                        w, 0, new Color(16, 42, 85)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);

                // Glowing neon cyan border
                g2.setColor(new Color(0, 229, 255, 200));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, 10, 10);

                // Left vertical neon indicator bar
                g2.setColor(new Color(0, 229, 255));
                g2.fillRoundRect(5, 6, 4, h - 12, 4, 4);
            } else if (model.isRollover()) {
                // Hover state: highlighted slate card
                g2.setColor(new Color(36, 48, 76));
                g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
                g2.setColor(new Color(70, 95, 145));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, 10, 10);
            } else {
                // Inactive state: clearly visible distinct tab pill with subtle border
                g2.setColor(new Color(25, 33, 52));
                g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
                g2.setColor(new Color(44, 58, 88));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, 10, 10);
            }

            // Draw Icon
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            g2.setColor(active ? UITheme.ACCENT_CYAN : new Color(185, 205, 235));
            g2.drawString(iconSymbol, 16, h / 2 + 5);

            // Draw Text
            g2.setFont(active ? new Font("Segoe UI", Font.BOLD, 12) : new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(active ? Color.WHITE : (model.isRollover() ? Color.WHITE : new Color(215, 228, 248)));
            g2.drawString(labelText, 42, h / 2 + 4);

            g2.dispose();
        }
    }
}
