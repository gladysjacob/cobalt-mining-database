import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * DashboardPanel - Overview and quick statistics
 */
@SuppressWarnings("serial")
public class DashboardPanel extends JPanel {
    
    private JLabel topCommodityLabel;
    private JLabel avgResourcesLabel;
    private JLabel dataQualityLabel;
    
    /**
     * Constructor - Initialize dashboard
     */
    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(CobaltMiningGUI.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create components
        JPanel headerPanel = createHeaderPanel();
        JPanel statsPanel = createStatsPanel();
        JPanel insightsPanel = createInsightsPanel();
        
        // Layout
        add(headerPanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(insightsPanel, BorderLayout.SOUTH);
        
        // Load data
        loadDashboardData();
    }
    
    /**
     * Create header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CobaltMiningGUI.PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Database Overview Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Click any card for detailed breakdown");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(CobaltMiningGUI.PRIMARY_COLOR);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    /**
     * Create statistics panel with cards
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setBackground(CobaltMiningGUI.BACKGROUND_COLOR);
        
        // Create stat cards
        JPanel sitesCard = createStatCard("Mining Sites", "0", CobaltMiningGUI.SECONDARY_COLOR, "sites");
        JPanel resourcesCard = createStatCard("Resources", "0", new Color(155, 89, 182), "resources");
        JPanel locPointsCard = createStatCard("Location Points", "0", new Color(230, 126, 34), "locations");
        JPanel geologicalCard = createStatCard("Geological Data", "0", new Color(26, 188, 156), "geological");
        
        panel.add(sitesCard);
        panel.add(resourcesCard);
        panel.add(locPointsCard);
        panel.add(geologicalCard);
        
        return panel;
    }
    
    /**
     * Create a stat card with context
     */
    private JPanel createStatCard(String title, String value, Color color, String cardType) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Make clickable
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(CobaltMiningGUI.TEXT_COLOR);
        
        // Click hint
        JLabel descLabel = new JLabel("Click for details");
        descLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        descLabel.setForeground(new Color(127, 140, 141));
        
        contentPanel.add(titleLabel, gbc);
        contentPanel.add(descLabel, gbc);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Add click listener
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDetailPopup(cardType, title);
            }
        });
        
        return card;
    }
    
    /**
     * Show detailed popup for clicked card
     */
    private void showDetailPopup(String cardType, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            title + " - Details", true);
        dialog.setSize(650, 550);
        dialog.setLocationRelativeTo(this);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        contentPanel.setBackground(new Color(245, 247, 250)); // Soft blue-gray background
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CobaltMiningGUI.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel(title + " Breakdown");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Detailed statistics and analysis");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        
        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1));
        headerTextPanel.setBackground(CobaltMiningGUI.PRIMARY_COLOR);
        headerTextPanel.add(titleLabel);
        headerTextPanel.add(subtitleLabel);
        
        headerPanel.add(headerTextPanel, BorderLayout.WEST);
        
        // Details area
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        detailsArea.setBackground(Color.WHITE);
        detailsArea.setForeground(new Color(52, 73, 94));
        detailsArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        detailsArea.setLineWrap(false);
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.setBackground(Color.WHITE);
        
        // Load details
        String details = loadDetails(cardType);
        detailsArea.setText(details);
        detailsArea.setCaretPosition(0);
        
        // Button panel with styled button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 247, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(CobaltMiningGUI.ACCENT_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(true);
        closeButton.setPreferredSize(new Dimension(120, 40));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(closeButton);
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Load detailed data for popup
     */
    private String loadDetails(String cardType) {
        StringBuilder sb = new StringBuilder();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs;
            
            switch(cardType) {
                case "sites":
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    sb.append("  SITES BY COMMODITY\n");
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                    rs = stmt.executeQuery(
                        "SELECT Commodity, COUNT(DISTINCT Site_ID) as cnt FROM SITE_COMMODITY " +
                        "GROUP BY Commodity ORDER BY cnt DESC");
                    while (rs.next()) {
                        sb.append(String.format("  %-20s %3d sites\n", 
                            rs.getString("Commodity"), rs.getInt("cnt")));
                    }
                    rs.close();
                    
                    sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    sb.append("  TOP 10 SITES BY RESOURCE COUNT\n");
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                    rs = stmt.executeQuery(
                        "SELECT s.Site_Name, COUNT(r.Resource_ID) as cnt " +
                        "FROM SITE s LEFT JOIN RESOURCE r ON s.Site_ID = r.Site_ID " +
                        "GROUP BY s.Site_ID, s.Site_Name ORDER BY cnt DESC LIMIT 10");
                    int rank = 1;
                    while (rs.next()) {
                        sb.append(String.format("  %2d. %-30s %3d resources\n", 
                            rank++, rs.getString("Site_Name"), rs.getInt("cnt")));
                    }
                    rs.close();
                    break;
                    
                case "resources":
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    sb.append("  RESOURCES BY MATERIAL TYPE\n");
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                    rs = stmt.executeQuery(
                        "SELECT Material, COUNT(*) as cnt FROM RESOURCE " +
                        "WHERE Material IS NOT NULL GROUP BY Material ORDER BY cnt DESC");
                    while (rs.next()) {
                        sb.append(String.format("  %-20s %3d estimates\n", 
                            rs.getString("Material"), rs.getInt("cnt")));
                    }
                    rs.close();
                    break;
                    
                case "geological":
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    sb.append("  OCCURRENCES BY TYPE\n");
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                    rs = stmt.executeQuery(
                        "SELECT Ftr_Type, COUNT(*) as cnt FROM GEOL_MIN_OCC " +
                        "WHERE Ftr_Type IS NOT NULL GROUP BY Ftr_Type ORDER BY cnt DESC");
                    while (rs.next()) {
                        sb.append(String.format("  %-25s %3d occurrences\n", 
                            rs.getString("Ftr_Type"), rs.getInt("cnt")));
                    }
                    rs.close();
                    
                    sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    sb.append("  TOP 10 COMMODITIES\n");
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                    rs = stmt.executeQuery(
                        "SELECT Commodity, COUNT(*) as cnt FROM GEOL_COMMODITY " +
                        "GROUP BY Commodity ORDER BY cnt DESC LIMIT 10");
                    rank = 1;
                    while (rs.next()) {
                        sb.append(String.format("  %2d. %-20s %3d occurrences\n", 
                            rank++, rs.getString("Commodity"), rs.getInt("cnt")));
                    }
                    rs.close();
                    break;
                    
                case "locations":
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    sb.append("  LOCATION POINTS BY COUNTY\n");
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                    rs = stmt.executeQuery(
                        "SELECT County, COUNT(*) as cnt FROM LOCATION_POINT_COUNTY " +
                        "GROUP BY County ORDER BY cnt DESC LIMIT 15");
                    while (rs.next()) {
                        sb.append(String.format("  %-30s %3d points\n", 
                            rs.getString("County"), rs.getInt("cnt")));
                    }
                    rs.close();
                    
                    sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    sb.append("  POINTS BY FEATURE TYPE\n");
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                    rs = stmt.executeQuery(
                        "SELECT Ftr_Type, COUNT(*) as cnt FROM LOCATION_POINT " +
                        "WHERE Ftr_Type IS NOT NULL GROUP BY Ftr_Type ORDER BY cnt DESC");
                    while (rs.next()) {
                        sb.append(String.format("  %-25s %3d points\n", 
                            rs.getString("Ftr_Type"), rs.getInt("cnt")));
                    }
                    rs.close();
                    break;
            }
            
            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            stmt.close();
            
        } catch (SQLException e) {
            sb.append("\nError loading details: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sb.toString();
    }
    
    /**
     * Create insights panel with meaningful information
     */
    private JPanel createInsightsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CobaltMiningGUI.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Key Insights");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(CobaltMiningGUI.PRIMARY_COLOR);
        
        // Insights panel
        JPanel insightsContent = new JPanel(new GridLayout(3, 1, 10, 10));
        insightsContent.setBackground(Color.WHITE);
        
        topCommodityLabel = new JLabel("Most tracked commodity: Loading...");
        topCommodityLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        avgResourcesLabel = new JLabel("Average resources per site: Loading...");
        avgResourcesLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        dataQualityLabel = new JLabel("Data completeness: Loading...");
        dataQualityLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        insightsContent.add(topCommodityLabel);
        insightsContent.add(avgResourcesLabel);
        insightsContent.add(dataQualityLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));
        refreshButton.setBackground(CobaltMiningGUI.ACCENT_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setOpaque(true);
        refreshButton.setBorderPainted(false);
        refreshButton.addActionListener(e -> {
            topCommodityLabel.setText("Loading...");
            avgResourcesLabel.setText("Loading...");
            dataQualityLabel.setText("Loading...");
            loadDashboardData();
            repaint();
            revalidate();
            JOptionPane.showMessageDialog(this,
                "Statistics refreshed successfully!",
                "Refresh Complete",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(refreshButton);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(insightsContent, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Load dashboard data from database
     */
    private void loadDashboardData() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            
            // Get counts for insights
            ResultSet rs = stmt.executeQuery(
                "SELECT " +
                "(SELECT COUNT(*) FROM SITE) AS sites, " +
                "(SELECT COUNT(*) FROM RESOURCE) AS resources"
            );
            
            if (rs.next()) {
                int sites = rs.getInt("sites");
                int resources = rs.getInt("resources");
                
                // Calculate average resources per site
                if (sites > 0) {
                    double avg = (double) resources / sites;
                    avgResourcesLabel.setText(String.format("Average resources per site: %.1f estimates", avg));
                }
            }
            rs.close();
            
            // Get most common commodity
            rs = stmt.executeQuery(
                "SELECT Commodity, COUNT(*) as count " +
                "FROM SITE_COMMODITY " +
                "GROUP BY Commodity " +
                "ORDER BY count DESC " +
                "LIMIT 1"
            );
            
            if (rs.next()) {
                String commodity = rs.getString("Commodity");
                int count = rs.getInt("count");
                topCommodityLabel.setText(String.format("Most tracked commodity: %s (found at %d sites)", 
                    commodity, count));
            }
            rs.close();
            
            // Calculate data quality
            rs = stmt.executeQuery(
                "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN Remarks IS NOT NULL AND Remarks != '' THEN 1 ELSE 0 END) as documented " +
                "FROM SITE"
            );
            
            if (rs.next()) {
                int total = rs.getInt("total");
                int documented = rs.getInt("documented");
                double quality = (double) documented / total * 100;
                dataQualityLabel.setText(String.format("Data completeness: %.0f%% of sites have documentation", quality));
            }
            rs.close();
            
            stmt.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading dashboard data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}