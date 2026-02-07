import javax.swing.*;
import java.awt.*;

/**
 * CobaltMiningGUI - Main application window
 */
@SuppressWarnings("serial")
public class CobaltMiningGUI extends JFrame {
    
    // Color scheme - Modern Professional Blue/Gray
    public static final Color PRIMARY_COLOR = new Color(44, 62, 80);      // Navy Blue
    public static final Color SECONDARY_COLOR = new Color(52, 152, 219);  // Light Blue
    public static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // Light Gray
    public static final Color TEXT_COLOR = new Color(52, 73, 94);         // Dark Gray
    public static final Color ACCENT_COLOR = new Color(39, 174, 96);      // Green
    
    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private TableViewerPanel tableViewerPanel;
    private GeologicalFilterPanel geologicalFilterPanel;
    private ResourceCleanerPanel resourceCleanerPanel;
    
    /**
     * Constructor - Sets up the main application window
     */
    public CobaltMiningGUI() {
        setTitle("Cobalt Mining Database - Investment Analysis Tool");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        
        // Test database connection on startup
        if (!testDatabaseConnection()) {
            JOptionPane.showMessageDialog(this,
                "Unable to connect to database.\nPlease check your MySQL server is running.",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Initialize components
        initializeComponents();
        
        // Set look and feel
        setApplicationLookAndFeel();
    }
    
    /**
     * Initialize all GUI components
     */
    private void initializeComponents() {
        // Set background color
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        
        // Create panels for each tab
        dashboardPanel = new DashboardPanel();
        tableViewerPanel = new TableViewerPanel();
        geologicalFilterPanel = new GeologicalFilterPanel();
        resourceCleanerPanel = new ResourceCleanerPanel();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", createIcon("dashboard"), dashboardPanel,
            "Overview and statistics");
        tabbedPane.addTab("Table Viewer", createIcon("table"), tableViewerPanel, 
            "View database tables");
        tabbedPane.addTab("Geological Filter", createIcon("filter"), geologicalFilterPanel,
            "Filter by commodity and feature type");
        tabbedPane.addTab("Resource Cleaner", createIcon("clean"), resourceCleanerPanel,
            "View cleaned resource indicators");
        
        // Layout
        setLayout(new BorderLayout(10, 10));
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add padding
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    /**
     * Create header panel with title and info
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Cobalt Mining Database");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Investment Opportunity Analysis - USGS Data");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        
        // Organize labels
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    /**
     * Create simple icon for tabs
     */
    private Icon createIcon(String type) {
        return new Icon() {
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Color color = SECONDARY_COLOR;
                if (type.equals("dashboard")) color = new Color(155, 89, 182); // Purple
                if (type.equals("filter")) color = ACCENT_COLOR;
                if (type.equals("clean")) color = new Color(230, 126, 34); // Orange
                
                g.setColor(color);
                g.fillRoundRect(x, y, 16, 16, 4, 4);
            }
        };
    }
    
    /**
     * Test database connection on startup
     */
    private boolean testDatabaseConnection() {
        try {
            return DatabaseConnection.testConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Set application look and feel
     */
    private void setApplicationLookAndFeel() {
        try {
            // Use system look and feel for native appearance
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }
    
    /**
     * Main method - Application entry point
     */
    public static void main(String[] args) {
        // Run GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            CobaltMiningGUI app = new CobaltMiningGUI();
            app.setVisible(true);
        });
    }
}