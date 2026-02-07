import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.sql.*;

/**
 * GeologicalFilterPanel - Filter geological occurrences by commodity and feature type
 */
@SuppressWarnings("serial")
public class GeologicalFilterPanel extends JPanel {
    
    private JComboBox<String> commoditySelector;
    private JComboBox<String> featureTypeSelector;
    private JButton filterButton;
    private JButton clearButton;
    private JTextField searchField;
    private JTable resultsTable;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    
    // Available commodities and feature types
    private final String[] COMMODITIES = {
        "cobalt", "copper", "nickel", "gold", "silver", "zinc", "lead"
    };
    
    private final String[] FEATURE_TYPES = {
        "Deposit", "Prospect", "Occurrence", "Mine", "Mining District"
    };
    
    /**
     * Constructor - Initialize the filter panel
     */
    public GeologicalFilterPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(CobaltMiningGUI.BACKGROUND_COLOR);
        
        // Create components
        JPanel controlPanel = createControlPanel();
        JPanel tablePanel = createTablePanel();
        JPanel statusPanel = createStatusPanel();
        
        // Add components
        add(controlPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create control panel with filters
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CobaltMiningGUI.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("Filter Geological Mineral Occurrences");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(CobaltMiningGUI.PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        panel.add(titleLabel, gbc);
        
        // Commodity label
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel commodityLabel = new JLabel("Commodity:");
        commodityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(commodityLabel, gbc);
        
        // Commodity dropdown
        gbc.gridx = 1;
        commoditySelector = new JComboBox<>(COMMODITIES);
        commoditySelector.setFont(new Font("Arial", Font.PLAIN, 14));
        commoditySelector.setPreferredSize(new Dimension(150, 30));
        panel.add(commoditySelector, gbc);
        
        // Feature type label
        gbc.gridx = 2;
        JLabel featureLabel = new JLabel("Feature Type:");
        featureLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(featureLabel, gbc);
        
        // Feature type dropdown
        gbc.gridx = 3;
        featureTypeSelector = new JComboBox<>(FEATURE_TYPES);
        featureTypeSelector.setFont(new Font("Arial", Font.PLAIN, 14));
        featureTypeSelector.setPreferredSize(new Dimension(150, 30));
        panel.add(featureTypeSelector, gbc);
        
        // Buttons
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        
        filterButton = new JButton("Apply Filter");
        filterButton.setFont(new Font("Arial", Font.BOLD, 14));
        filterButton.setBackground(CobaltMiningGUI.SECONDARY_COLOR);
        filterButton.setForeground(Color.WHITE);
        filterButton.setFocusPainted(false);
        filterButton.setOpaque(true);
        filterButton.setBorderPainted(false);
        filterButton.addActionListener(e -> applyFilter());
        panel.add(filterButton, gbc);
        
        gbc.gridx = 2;
        clearButton = new JButton("Clear Results");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 14));
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        clearButton.setBorderPainted(false);
        clearButton.addActionListener(e -> clearResults());
        panel.add(clearButton, gbc);
        
        // Search field (row 3)
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel searchLabel = new JLabel("Search Results:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(searchLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                performSearch();
            }
        });
        panel.add(searchField, gbc);
        
        return panel;
    }
    
    /**
     * Create table panel for results
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CobaltMiningGUI.BACKGROUND_COLOR);
        
        // Create results table
        resultsTable = new JTable();
        resultsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        resultsTable.setRowHeight(25);
        resultsTable.setGridColor(new Color(189, 195, 199));
        resultsTable.setSelectionBackground(CobaltMiningGUI.SECONDARY_COLOR);
        resultsTable.setSelectionForeground(Color.WHITE);
        
        // Enable column sorting
        resultsTable.setAutoCreateRowSorter(true);
        
        // Style header
        JTableHeader header = resultsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(CobaltMiningGUI.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        
        // Scroll pane
        scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(CobaltMiningGUI.PRIMARY_COLOR, 2));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create status panel
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel("Select commodity and feature type, then click 'Apply Filter'");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(CobaltMiningGUI.TEXT_COLOR);
        
        panel.add(statusLabel);
        
        return panel;
    }
    
    /**
     * Apply filter - Call stored procedure
     */
    private void applyFilter() {
        String commodity = (String) commoditySelector.getSelectedItem();
        String featureType = (String) featureTypeSelector.getSelectedItem();
        
        statusLabel.setText("Filtering for " + commodity + " " + featureType + "...");
        statusLabel.setForeground(CobaltMiningGUI.SECONDARY_COLOR);
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Prepare callable statement for stored procedure
            CallableStatement cstmt = conn.prepareCall(
                "{CALL filter_geol_by_commodity_type(?, ?)}"
            );
            
            // Set parameters
            cstmt.setString(1, commodity);
            cstmt.setString(2, featureType);
            
            // Execute procedure
            ResultSet rs = cstmt.executeQuery();
            
            // Get column information
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Create column names
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            
            // Create table model
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            
            // Populate table
            int rowCount = 0;
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
                rowCount++;
            }
            
            // Set model
            resultsTable.setModel(model);
            
            // Update status
            if (rowCount == 0) {
                statusLabel.setText("No results found for " + commodity + " " + featureType);
                statusLabel.setForeground(new Color(230, 126, 34)); // Orange
            } else {
                statusLabel.setText("Found " + rowCount + " results for " + commodity + " " + featureType);
                statusLabel.setForeground(CobaltMiningGUI.ACCENT_COLOR);
            }
            
            // Close resources
            rs.close();
            cstmt.close();
            
        } catch (SQLException e) {
            statusLabel.setText("Error executing filter: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                "Database Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Clear results table
     */
    private void clearResults() {
        resultsTable.setModel(new DefaultTableModel());
        searchField.setText(""); // Clear search field too
        statusLabel.setText("Results cleared. Select filters and click 'Apply Filter'");
        statusLabel.setForeground(CobaltMiningGUI.TEXT_COLOR);
    }
    
    /**
     * Perform search/filter on current results
     */
    private void performSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        
        if (resultsTable.getModel().getRowCount() == 0) {
            return; // No data to search
        }
        
        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        resultsTable.setRowSorter(sorter);
        
        if (searchText.length() == 0) {
            sorter.setRowFilter(null); // Show all rows
        } else {
            // Search across all columns
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
        
        // Update status with filtered count
        int visibleRows = resultsTable.getRowCount();
        int totalRows = model.getRowCount();
        if (visibleRows < totalRows) {
            statusLabel.setText("Showing " + visibleRows + " of " + totalRows + " results (filtered)");
            statusLabel.setForeground(CobaltMiningGUI.ACCENT_COLOR);
        }
    }
}