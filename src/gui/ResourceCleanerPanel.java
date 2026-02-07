import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.sql.*;

/**
 * ResourceCleanerPanel - Display cleaned resource indicators
 * CSP5b: Calls stored procedure clean_resource_indicators
 */
@SuppressWarnings("serial")
public class ResourceCleanerPanel extends JPanel {
    
    private JButton loadButton;
    private JButton refreshButton;
    private JButton exportButton;
    private JTextField searchField;
    private JTable resultsTable;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JLabel infoLabel;
    
    /**
     * Constructor - Initialize the resource cleaner panel
     */
    public ResourceCleanerPanel() {
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
     * Create control panel
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
        JLabel titleLabel = new JLabel("Clean USGS Indicator Values from Resources");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(CobaltMiningGUI.PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(titleLabel, gbc);
        
        // Info label with help text
        gbc.gridy = 1;
        infoLabel = new JLabel("<html><i>Indicator values append 0.00111 to numbers (e.g., 0.05 becomes 0.05111)<br>" +
                              "This tool displays resources with indicator values removed<br><br>" +
                              "<b>Note:</b> Negative Mat_Amnt values represent total ore tonnage (size of ore body), " +
                              "while Contained_Amnt shows actual metal content (USGS convention).</i></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(CobaltMiningGUI.TEXT_COLOR);
        panel.add(infoLabel, gbc);
        
        // Buttons
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        
        loadButton = new JButton("Load Cleaned Resources");
        loadButton.setFont(new Font("Arial", Font.BOLD, 14));
        loadButton.setBackground(CobaltMiningGUI.SECONDARY_COLOR);
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.setOpaque(true);
        loadButton.setBorderPainted(false);
        loadButton.addActionListener(e -> loadCleanedResources());
        panel.add(loadButton, gbc);
        
        gbc.gridx = 1;
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshButton.setBackground(CobaltMiningGUI.ACCENT_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setOpaque(true);
        refreshButton.setBorderPainted(false);
        refreshButton.addActionListener(e -> loadCleanedResources());
        panel.add(refreshButton, gbc);
        
        gbc.gridx = 2;
        exportButton = new JButton("Clear");
        exportButton.setFont(new Font("Arial", Font.PLAIN, 14));
        exportButton.setBackground(new Color(149, 165, 166));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.setOpaque(true);
        exportButton.setBorderPainted(false);
        exportButton.addActionListener(e -> clearResults());
        panel.add(exportButton, gbc);
        
        // Search field (row 3)
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel searchLabel = new JLabel("Search Results:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(searchLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        searchField = new JTextField(25);
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
     * Create table panel
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
        
        statusLabel = new JLabel("Click 'Load Cleaned Resources' to display resources with cleaned indicator values");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(CobaltMiningGUI.TEXT_COLOR);
        
        panel.add(statusLabel);
        
        return panel;
    }
    
    /**
     * Load cleaned resources - Call stored procedure
     */
    private void loadCleanedResources() {
        statusLabel.setText("Loading cleaned resource data...");
        statusLabel.setForeground(CobaltMiningGUI.SECONDARY_COLOR);
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Prepare callable statement for stored procedure
            CallableStatement cstmt = conn.prepareCall(
                "{CALL clean_resource_indicators()}"
            );
            
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
                    Object value = rs.getObject(i + 1);
                    
                    // Format the "contained" column to remove trailing zeros
                    if (columnNames[i].equals("contained") && value != null) {
                        if (value instanceof Number) {
                            double num = ((Number) value).doubleValue();
                            // Remove trailing zeros: format as string then parse back
                            String formatted = String.format("%.4f", num).replaceAll("0*$", "").replaceAll("\\.$", "");
                            row[i] = formatted;
                        } else {
                            row[i] = value;
                        }
                    } else {
                        row[i] = value;
                    }
                }
                model.addRow(row);
                rowCount++;               
            }
            
            // Set model
            resultsTable.setModel(model);
            
            // Update status
            statusLabel.setText("Loaded " + rowCount + " resource records with cleaned indicator values");
            statusLabel.setForeground(CobaltMiningGUI.ACCENT_COLOR);
            
            // Close resources
            rs.close();
            cstmt.close();
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading cleaned resources: " + e.getMessage());
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
        statusLabel.setText("Results cleared. Click 'Load Cleaned Resources' to reload");
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
            statusLabel.setText("Showing " + visibleRows + " of " + totalRows + " resources (filtered)");
            statusLabel.setForeground(CobaltMiningGUI.ACCENT_COLOR);
        }
    }
}