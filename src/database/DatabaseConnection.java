import java.sql.*;
import com.mysql.cj.jdbc.MysqlDataSource;


public class DatabaseConnection {
    
    // Database connection parameters
    private static final String DB_SERVER = "localhost";
    private static final int DB_PORT = 3306;
    private static final String DB_NAME = "cobalt_mining";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_mysql_password"; // CHANGE THIS TO YOUR DB PASSWORD!
    
    private static Connection connection = null;
    
    /**
     * Get database connection using DataSource
     * 
     * @return Connection to cobalt_mining database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Create DataSource
            MysqlDataSource dataSource = new MysqlDataSource();
            
            // Configure connection properties
            dataSource.setServerName(DB_SERVER);
            dataSource.setPort(DB_PORT);
            dataSource.setDatabaseName(DB_NAME);
            dataSource.setUser(DB_USER);
            dataSource.setPassword(DB_PASSWORD);
            
            // Get connection from DataSource
            connection = dataSource.getConnection();
            
            System.out.println("Connected to database via DataSource");
        }
        return connection;
    }
    
    /**
     * Close database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test connection
     * 
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get database metadata
     * Useful for displaying database info in GUI
     * 
     * @return DatabaseMetaData object
     * @throws SQLException if error occurs
     */
    public static DatabaseMetaData getMetaData() throws SQLException {
        return getConnection().getMetaData();
    }
}