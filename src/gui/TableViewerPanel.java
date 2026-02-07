import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.RowFilter;
import java.awt.*;
import java.sql.*;

/**
 * TableViewerPanel - Display database tables
 */
@SuppressWarnings("serial")
public class TableViewerPanel extends JPanel {
    
    private JComboBox<String> tableSelector;
    private JButton loadButton;
    private JButton refreshButton;
    private JTextField searchField;
    private JTable dataTable;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JPanel statsPanel;
    private JLabel totalRowsLabel;
    private JLabel selectedTableLabel;
    
    // Table names mapping
    private final String[] TABLE_NAMES = {
        "Location Points (Loc_pt)",
        "Geological Occurrences (GeolMinOcc)", 
        "Location Polygons (Loc_Poly)",
        "Production Records",
        "Resource Estimates"
    };
    
    /**
     * Constructor - Initialize the table viewer panel
     */
    public TableViewerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(CobaltMiningGUI.BACKGROUND_COLOR);
        
        // Create components
        JPanel controlPanel = createControlPanel();
        JPanel tablePanel = createTablePanel();
        JPanel bottomPanel = createBottomPanel();
        
        // Add components
        add(controlPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Load database statistics on startup
        loadDatabaseStatistics();
    }
    
    /**
     * Create control panel with table selector and buttons
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CobaltMiningGUI.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Label
        JLabel label = new JLabel("Select Table:");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(CobaltMiningGUI.TEXT_COLOR);
        
        // Dropdown selector
        tableSelector = new JComboBox<>(TABLE_NAMES);
        tableSelector.setFont(new Font("Arial", Font.PLAIN, 14));
        tableSelector.setPreferredSize(new Dimension(300, 30));
        
        // Load button
        loadButton = new JButton("Load Table");
        loadButton.setFont(new Font("Arial", Font.BOLD, 14));
        loadButton.setBackground(CobaltMiningGUI.SECONDARY_COLOR);
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.setOpaque(true);
        loadButton.setBorderPainted(false);
        loadButton.addActionListener(e -> loadSelectedTable());
        
        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshButton.setBackground(CobaltMiningGUI.ACCENT_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setOpaque(true);
        refreshButton.setBorderPainted(false);
        refreshButton.addActionListener(e -> loadSelectedTable());
        
        // Search field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addActionListener(e -> performSearch());
        
        // Add key listener for real-time search
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                performSearch();
            }
        });
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Help button (explains negative values and features)
        JButton helpButton = new JButton("ℹ Help");
        helpButton.setFont(new Font("Arial", Font.PLAIN, 12));
        helpButton.setBackground(new Color(52, 152, 219)); // Blue
        helpButton.setForeground(Color.WHITE);
        helpButton.setFocusPainted(false);
        helpButton.setOpaque(true);
        helpButton.setBorderPainted(false);
        helpButton.addActionListener(e -> showHelp());
        
        panel.add(label);
        panel.add(tableSelector);
        panel.add(loadButton);
        panel.add(refreshButton);
        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(helpButton);
        
        return panel;
    }
    
    /**
     * Show help dialog explaining table features and negative values
     */
    private void showHelp() {
        String helpText = "<html><body style='width: 450px; padding: 10px;'>" +
            "<h2 style='color: #2C3E50;'>Table Viewer Help</h2>" +
            
            "<h3 style='color: #3498DB;'>General Features:</h3>" +
            "<ul>" +
            "<li><b>Sortable Columns:</b> Click any column header to sort</li>" +
            "<li><b>Row Details:</b> Double-click any row for detailed view</li>" +
            "<li><b>Color Coding:</b> Resource table's Contained_Amnt column is color-coded by value</li>" +
            "</ul>" +
            
            "<h3 style='color: #3498DB;'>Resource Estimates - Contained_Amnt Color Legend:</h3>" +
            "<ul>" +
            "<li><span style='background-color: rgb(34,139,34); color: white; padding: 2px 8px;'>&nbsp;Dark Green&nbsp;</span> - Very High (&gt;1 billion pounds)</li>" +
            "<li><span style='background-color: rgb(144,238,144); padding: 2px 8px;'>&nbsp;Light Green&nbsp;</span> - High (&gt;100 million pounds)</li>" +
            "<li><span style='background-color: rgb(255,255,204); padding: 2px 8px;'>&nbsp;Yellow&nbsp;</span> - Medium (&gt;10 million pounds)</li>" +
            "<li><span style='background-color: rgb(224,242,255); padding: 2px 8px;'>&nbsp;Light Blue&nbsp;</span> - Low (&gt;1 million pounds)</li>" +
            "</ul>" +
            
            "<h3 style='color: #E74C3C;'>⚠️ Understanding Negative Values:</h3>" +
            "<p><b>Resource Estimates Table:</b> You may notice negative values in the <b>Mat_Amnt</b> " +
            "(Material Amount) column.</p>" +
            
            "<p><b>This is a USGS data convention</b> where negative values represent the <b>total ore " +
            "tonnage</b> (size of the ore body to be mined), not the metal content.</p>" +
            
            "<p><b>Example:</b></p>" +
            "<ul>" +
            "<li><b>Mat_Amnt:</b> -1,130,000,000 short tons <i>(total ore body)</i></li>" +
            "<li><b>Contained_Amnt:</b> 13,366,000,000 pounds <i>(actual copper metal)</i></li>" +
            "</ul>" +
            
            "<p>Think of it as: <i>\"We need to mine 1.13 billion tons of ore to extract " +
            "13.4 billion pounds of copper.\"</i></p>" +
            
            "<p style='margin-top: 10px; padding: 8px; background-color: #ECF0F1; border-left: 4px solid #3498DB;'>" +
            "<b>Note:</b> The <b>Contained_Amnt</b> column shows the actual metal content and is used " +
            "for color-coding to identify high-value deposits.</p>" +
            
            "</body></html>";
        
        JOptionPane.showMessageDialog(this,
            helpText,
            "Table Viewer Help",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Create panel for displaying table data
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CobaltMiningGUI.BACKGROUND_COLOR);
        
        // Create empty table
        dataTable = new JTable();
        dataTable.setFont(new Font("Arial", Font.PLAIN, 12));
        dataTable.setRowHeight(25);
        dataTable.setGridColor(new Color(189, 195, 199));
        dataTable.setSelectionBackground(CobaltMiningGUI.SECONDARY_COLOR);
        dataTable.setSelectionForeground(Color.WHITE);
        
        // Enable column sorting
        dataTable.setAutoCreateRowSorter(true);
        
        // Style table header
        JTableHeader header = dataTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(CobaltMiningGUI.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        
        // Scroll pane
        scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(CobaltMiningGUI.PRIMARY_COLOR, 2));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create bottom panel with status and statistics
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Status label
        statusLabel = new JLabel("Select a table and click 'Load Table'");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(CobaltMiningGUI.TEXT_COLOR);
        
        // Statistics panel
        statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(Color.WHITE);
        
        totalRowsLabel = new JLabel("Total Rows: 0");
        totalRowsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        totalRowsLabel.setForeground(CobaltMiningGUI.ACCENT_COLOR);
        
        selectedTableLabel = new JLabel("");
        selectedTableLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        selectedTableLabel.setForeground(CobaltMiningGUI.TEXT_COLOR);
        
        statsPanel.add(selectedTableLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(totalRowsLabel);
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(statsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Load database statistics
     */
    private void loadDatabaseStatistics() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            
            // Get total counts from main tables
            ResultSet rs = stmt.executeQuery(
                "SELECT " +
                "(SELECT COUNT(*) FROM SITE) AS sites, " +
                "(SELECT COUNT(*) FROM LOCATION_POINT) AS points, " +
                "(SELECT COUNT(*) FROM RESOURCE) AS resources, " +
                "(SELECT COUNT(*) FROM PRODUCTION) AS production, " +
                "(SELECT COUNT(*) FROM GEOL_MIN_OCC) AS geological"
            );
            
            if (rs.next()) {
                int sites = rs.getInt("sites");
                int points = rs.getInt("points");
                int resources = rs.getInt("resources");
                int production = rs.getInt("production");
                int geological = rs.getInt("geological");
                
                selectedTableLabel.setText(String.format(
                    "DB Stats: %d Sites, %d Points, %d Resources, %d Production, %d Geological",
                    sites, points, resources, production, geological
                ));
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            selectedTableLabel.setText("Could not load statistics");
        }
    }
    
    /**
     * Load selected table from database
     */
    private void loadSelectedTable() {
        int selectedIndex = tableSelector.getSelectedIndex();
        String tableName = TABLE_NAMES[selectedIndex];
        
        statusLabel.setText("Loading " + tableName + "...");
        statusLabel.setForeground(CobaltMiningGUI.SECONDARY_COLOR);
        
        // Execute query based on selection
        switch (selectedIndex) {
            case 0:
                loadLocationPoints();
                break;
            case 1:
                loadGeologicalOccurrences();
                break;
            case 2:
                loadLocationPolygons();
                break;
            case 3:
                loadProduction();
                break;
            case 4:
                loadResources();
                break;
        }
    }
    
    /**
     * Load Location Points (Loc_pt)
     */
    private void loadLocationPoints() {
        String query = 
            "SELECT " +
            "    lp.Loc_Point_ID AS OBJECTID, " +
            "    lp.Site_ID, " +
            "    lp.Ftr_ID, " +
            "    lp.Ftr_Name, " +
            "    GROUP_CONCAT(DISTINCT lpon.Other_Name SEPARATOR '; ') AS Other_Name, " +
            "    lp.Last_Updt, " +
            "    lp.Ftr_Group, " +
            "    lp.Ftr_Type, " +
            "    GROUP_CONCAT(DISTINCT lpc.Commodity SEPARATOR '; ') AS Commodity, " +
            "    lp.Latitude AS Lat_WGS84, " +
            "    lp.Longitude AS Long_WGS84, " +
            "    lp.Point_Definition AS Pt_Def, " +
            "    lp.Poly_Definition AS Poly_Def, " +
            "    GROUP_CONCAT(DISTINCT lpco.County SEPARATOR '; ') AS County, " +
            "    lp.Loc_Scale, " +
            "    lp.Loc_Date, " +
            "    lp.Ref_ID, " +
            "    lp.Remarks " +
            "FROM LOCATION_POINT lp " +
            "LEFT JOIN LOCATION_POINT_OTHER_NAMES lpon ON lp.Loc_Point_ID = lpon.Loc_Point_ID " +
            "LEFT JOIN LOCATION_POINT_COMMODITY lpc ON lp.Loc_Point_ID = lpc.Loc_Point_ID " +
            "LEFT JOIN LOCATION_POINT_COUNTY lpco ON lp.Loc_Point_ID = lpco.Loc_Point_ID " +
            "GROUP BY lp.Loc_Point_ID " +
            "ORDER BY lp.Loc_Point_ID";
        
        executeQuery(query, "Location Points");
    }
    
    /**
     * Load Geological Mineral Occurrences (GeolMinOcc)
     */
    private void loadGeologicalOccurrences() {
        String query =
            "SELECT " +
            "    g.Geol_Min_ID AS OID, " +
            "    g.Site_ID, " +
            "    g.Ftr_ID, " +
            "    g.Ftr_Name, " +
            "    g.Last_Updt, " +
            "    g.Ftr_Type, " +
            "    GROUP_CONCAT(DISTINCT gc.Commodity SEPARATOR '; ') AS Commodity, " +
            "    GROUP_CONCAT(DISTINCT gv.Value_Material SEPARATOR '; ') AS Value_Mat, " +
            "    GROUP_CONCAT(DISTINCT ga.Assoc_Material SEPARATOR '; ') AS Assoc_Mat, " +
            "    GROUP_CONCAT(DISTINCT gms.Mineralization_Style SEPARATOR '; ') AS Min_Style, " +
            "    GROUP_CONCAT(DISTINCT gma.Mineralization_Age SEPARATOR '; ') AS Min_Age, " +
            "    g.Host_Age, " +
            "    g.Host_Name, " +
            "    GROUP_CONCAT(DISTINCT ghl.Host_Lithology SEPARATOR '; ') AS Host_Litho, " +
            "    GROUP_CONCAT(DISTINCT galt.Alteration_Type SEPARATOR '; ') AS Alteration, " +
            "    g.Ref_ID, " +
            "    g.Remarks " +
            "FROM GEOL_MIN_OCC g " +
            "LEFT JOIN GEOL_COMMODITY gc ON g.Geol_Min_ID = gc.Geol_Min_ID " +
            "LEFT JOIN GEOL_VALUE_MAT gv ON g.Geol_Min_ID = gv.Geol_Min_ID " +
            "LEFT JOIN GEOL_ASSOC_MAT ga ON g.Geol_Min_ID = ga.Geol_Min_ID " +
            "LEFT JOIN GEOL_MIN_STYLE gms ON g.Geol_Min_ID = gms.Geol_Min_ID " +
            "LEFT JOIN GEOL_MIN_AGE gma ON g.Geol_Min_ID = gma.Geol_Min_ID " +
            "LEFT JOIN GEOL_HOST_LITHO ghl ON g.Geol_Min_ID = ghl.Geol_Min_ID " +
            "LEFT JOIN GEOL_ALTERATION galt ON g.Geol_Min_ID = galt.Geol_Min_ID " +
            "GROUP BY g.Geol_Min_ID " +
            "ORDER BY g.Geol_Min_ID";
        
        executeQuery(query, "Geological Mineral Occurrences");
    }
    
    /**
     * Load Location Polygons (Loc_Poly)
     */
    private void loadLocationPolygons() {
        String query =
            "SELECT " +
            "    Loc_Poly_ID AS OBJECTID, " +
            "    Site_ID, " +
            "    Ftr_ID, " +
            "    Ftr_Name, " +
            "    Last_Updt, " +
            "    Remarks, " +
            "    Area_SqKm, " +
            "    Area_Acres " +
            "FROM LOCATION_POLYGON " +
            "ORDER BY Loc_Poly_ID";
        
        executeQuery(query, "Location Polygons");
    }
    
    /**
     * Load Production Records
     */
    private void loadProduction() {
        String query =
            "SELECT " +
            "    p.Prod_ID AS OBJECTID, " +
            "    p.Site_ID, " +
            "    p.Ftr_ID, " +
            "    p.Ftr_Name, " +
            "    p.Last_Updt, " +
            "    p.Assoc_Deposit AS Assoc_Dep, " +
            "    p.Material, " +
            "    p.Year_From, " +
            "    p.Year_To, " +
            "    p.Mat_Type, " +
            "    p.Mat_Amnt, " +
            "    p.Mat_Units, " +
            "    p.Grade, " +
            "    p.Grade_Unit, " +
            "    p.Cutoff_Grade AS CutOffGrad, " +
            "    p.Cutoff_Unit AS CutOffUnit, " +
            "    p.Contained_Amnt AS Contained, " +
            "    p.Contained_Units AS Cont_Units, " +
            "    p.Recovery_Amnt AS Rcvry_Amt, " +
            "    p.Recovery_Units AS Rcvry_Unit, " +
            "    p.Prod_Value AS Prod_USD, " +
            "    p.Ref_Detail, " +
            "    GROUP_CONCAT(DISTINCT pr.Ref_ID SEPARATOR '; ') AS Ref_ID, " +
            "    p.Remarks " +
            "FROM PRODUCTION p " +
            "LEFT JOIN PROD_REFS pr ON p.Prod_ID = pr.Prod_ID " +
            "GROUP BY p.Prod_ID " +
            "ORDER BY p.Prod_ID";
        
        executeQuery(query, "Production Records");
    }
    
    /**
     * Load Resource Estimates
     */
    private void loadResources() {
        String query =
            "SELECT " +
            "    r.Resource_ID AS OBJECTID, " +
            "    r.Site_ID, " +
            "    r.Ftr_ID, " +
            "    r.Ftr_Name, " +
            "    r.Last_Updt, " +
            "    r.Material, " +
            "    r.Resource_Date AS Rsrc_Date, " +
            "    r.Mat_Type, " +
            "    r.Mat_Amnt, " +
            "    r.Mat_Units, " +
            "    r.Grade, " +
            "    r.Grade_Unit, " +
            "    r.Cutoff_Grade AS CutOffGrad, " +
            "    r.Cutoff_Unit AS CutOffUnit, " +
            "    r.Contained_Amnt AS Contained, " +
            "    r.Contained_Units AS Cont_Units, " +
            "    r.Resource_Class AS Rsrc_Class, " +
            "    r.Resource_Descr AS Rsrc_Descr, " +
            "    r.Resource_Code AS Rsrc_Code, " +
            "    r.Ref_Detail, " +
            "    GROUP_CONCAT(DISTINCT rr.Ref_ID SEPARATOR '; ') AS Ref_ID, " +
            "    r.Remarks " +
            "FROM RESOURCE r " +
            "LEFT JOIN RESOURCE_REFS rr ON r.Resource_ID = rr.Resource_ID " +
            "GROUP BY r.Resource_ID " +
            "ORDER BY r.Resource_ID";
        
        executeQueryWithColorCoding(query, "Resource Estimates");
    }
    
    /**
     * Execute query with color-coded rows for Resources
     */
    private void executeQueryWithColorCoding(String query, String tableName) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            // Get column information
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Create column names array
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            
            // Create custom table model for color coding
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int column) {
                    if (getRowCount() > 0) {
                        Object value = getValueAt(0, column);
                        if (value != null) {
                            return value.getClass();
                        }
                    }
                    return Object.class;
                }
            };
            
            // Populate table with data
            int rowCount = 0;
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
                rowCount++;
            }
            
            // Set model to table
            dataTable.setModel(model);
            
            // Create the color-coding renderer
            javax.swing.table.DefaultTableCellRenderer colorRenderer = new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    // Reset to default colors
                    c.setForeground(Color.BLACK);
                    c.setBackground(Color.WHITE);
                    
                    if (!isSelected) {
                        // Find the "Contained_Amnt" column
                        int containedCol = -1;
                        for (int i = 0; i < table.getColumnCount(); i++) {
                            String colName = table.getColumnName(i);
                            if (colName.equals("Contained_Amnt") || colName.equals("Contained")) {
                                containedCol = i;
                                break;
                            }
                        }
                        
                        // Only apply color to the Contained_Amnt column itself
                        if (containedCol != -1 && column == containedCol) {
                            Object containedValue = table.getValueAt(row, column);
                            if (containedValue != null) {
                                try {
                                    double contained = Double.parseDouble(containedValue.toString());
                                    
                                    // Color coding based on data ranges:
                                    if (contained > 1000000000) {  // 1 billion
                                        c.setBackground(new Color(34, 139, 34)); // Dark green
                                        c.setForeground(Color.WHITE); 
                                    } else if (contained > 100000000) {  // 100 million
                                        c.setBackground(new Color(144, 238, 144)); // Light green
                                        c.setForeground(Color.BLACK);
                                    } else if (contained > 10000000) {  // 10 million
                                        c.setBackground(new Color(255, 255, 204)); // Light yellow
                                        c.setForeground(Color.BLACK);
                                    } else if (contained > 1000000) {  // 1 million
                                        c.setBackground(new Color(224, 242, 255)); // Light blue
                                        c.setForeground(Color.BLACK);
                                    } else {
                                        c.setBackground(Color.WHITE);
                                        c.setForeground(Color.BLACK);
                                    }
                                } catch (NumberFormatException e) {
                                    // If can't parse, use default white
                                    c.setBackground(Color.WHITE);
                                    c.setForeground(Color.BLACK);
                                }
                            }
                        }
                    }
                    
                    return c;
                }
            };
            
            // Apply renderer to all column types
            dataTable.setDefaultRenderer(Object.class, colorRenderer);
            dataTable.setDefaultRenderer(Number.class, colorRenderer);
            dataTable.setDefaultRenderer(Double.class, colorRenderer);
            dataTable.setDefaultRenderer(Float.class, colorRenderer);
            dataTable.setDefaultRenderer(Integer.class, colorRenderer);
            dataTable.setDefaultRenderer(String.class, colorRenderer);
            
            int containedCol = -1;
            for (int i = 0; i < model.getColumnCount(); i++) {
                String colName = model.getColumnName(i);
                if (colName.equals("Contained_Amnt") || colName.equals("Contained")) {
                    containedCol = i;
                    break;
                }
            }
            System.out.println("Contained column index: " + containedCol);
            
            if (containedCol != -1 && rowCount > 0) {
                System.out.println("Sample Contained values:");
                for (int i = 0; i < Math.min(5, rowCount); i++) {
                    Object val = model.getValueAt(i, containedCol);
                    System.out.println("  Row " + i + ": " + val);
                }
            }
            
            // Force table to repaint with new renderer
            dataTable.repaint();
            
            // Update status
            statusLabel.setText(tableName + " loaded successfully - " + rowCount + " rows (Contained_Amnt column color-coded by value)");
            statusLabel.setForeground(CobaltMiningGUI.ACCENT_COLOR);
            totalRowsLabel.setText("Total Rows: " + rowCount);
            
            // Close resources
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading table: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                "Database Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Execute SQL query and display results in table
     */
    private void executeQuery(String query, String tableName) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            // Get column information
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Create column names array
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            
            // Create table model
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            
            // Populate table with data
            int rowCount = 0;
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
                rowCount++;
            }
            
            // Set model to table
            dataTable.setModel(model);
            
            // Update status and statistics
            statusLabel.setText(tableName + " loaded successfully - " + rowCount + " rows");
            statusLabel.setForeground(CobaltMiningGUI.ACCENT_COLOR);
            totalRowsLabel.setText("Total Rows: " + rowCount);
            
            // Close resources
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading table: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                "Database Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Perform search/filter on current table data
     */
    private void performSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        
        if (dataTable.getModel().getRowCount() == 0) {
            return; // No data to search
        }
        
        DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = 
            new javax.swing.table.TableRowSorter<>(model);
        dataTable.setRowSorter(sorter);
        
        if (searchText.length() == 0) {
            sorter.setRowFilter(null); // Show all rows
        } else {
            // Search across all columns
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
        
        // Update status with filtered count
        int visibleRows = dataTable.getRowCount();
        int totalRows = model.getRowCount();
        if (visibleRows < totalRows) {
            statusLabel.setText("Showing " + visibleRows + " of " + totalRows + " rows (filtered)");
            statusLabel.setForeground(CobaltMiningGUI.ACCENT_COLOR);
        }
    }
}