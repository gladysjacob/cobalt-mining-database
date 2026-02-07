import java.sql.*;
import java.io.*;
import java.util.*;

/**
 * DatabaseImporter - Imports USGS Cobalt Mining data into MySQL database
 * Note: Not used for final submission.
 * Note: Final Data can be imported using CobaltMining_CompleteDB.sql
 */
public class DatabaseImporter {
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost/cobalt_mining";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Aduthala7890!";
    
    // CSV file paths
    private static final String CSV_PATH = "/Users/gladysjacob/Library/CloudStorage/OneDrive-Personal/MUFall2025/CSE385/FinalProject/";
    
    private Connection connect = null;
    private Statement statement = null;
    
    /**
     * Main method - runs the complete import process
     */
    public static void main(String[] args) {
        System.out.println("COBALT MINING DATABASE - DATA IMPORT");
        
        DatabaseImporter importer = new DatabaseImporter();
        
        try {
            importer.connectToDatabase();
            importer.fixColumnSizes();
            importer.importAllData();
            importer.verifyImport();
            System.out.println("\nIMPORT COMPLETED SUCCESSFULLY!");
            
        } catch (Exception e) {
            System.err.println("\nERROR during import:");
            e.printStackTrace();
        } finally {
            importer.closeConnection();
        }
    }
    
    /**
     * Connect to MySQL database
     */
    private void connectToDatabase() throws SQLException {
        System.out.println("Connecting to database...");
        connect = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        statement = connect.createStatement();
        System.out.println("Connected to cobalt_mining database\n");
    }
    
    /**
     * Fix column sizes that are too small
     */
    private void fixColumnSizes() throws SQLException {
        System.out.println("Adjusting column sizes...");
        
        String[] alterCommands = {
            "ALTER TABLE REFERENCE MODIFY Ref_ID VARCHAR(500)",
            "ALTER TABLE LOCATION_POINT MODIFY Ref_ID VARCHAR(500)",
            "ALTER TABLE LOCATION_POLYGON_SW MODIFY Ref_ID VARCHAR(500)",
            "ALTER TABLE GEOL_MIN_OCC MODIFY Ref_ID VARCHAR(500)",
            "ALTER TABLE HISTORY MODIFY Ref_ID VARCHAR(500)",
            "ALTER TABLE DESCR_SUMMARY MODIFY Ref_ID VARCHAR(500)",
            "ALTER TABLE RESOURCE_REFS MODIFY Ref_ID VARCHAR(500)",
            "ALTER TABLE PROD_REFS MODIFY Ref_ID VARCHAR(500)"
        };
        
        for (String cmd : alterCommands) {
            try {
                statement.execute(cmd);
            } catch (SQLException e) {
                // Ignores if already correct size
            }
        }
        
        System.out.println("Column sizes adjusted\n");
    }
    
    /**
     * Import all data in correct order
     */
    private void importAllData() throws Exception {
        System.out.println("IMPORTING STRONG ENTITIES");
        
        importSites();
        importReferences();
        
        System.out.println("IMPORTING WEAK ENTITIES");
        
        importGeolMinOcc();
        importLocationPoints();
        importLocationPolygons();
        importLocationPolygonsSW();
        importResources();
        importProduction();
        importHistory();
        importDepositModels();
        importDescrSummary();
        
        System.out.println("IMPORTING JUNCTION TABLES (Multivalued Attributes)");
        
        importSiteCommodities();
        importSiteOtherNames();
        importLocationPointCommodities();
        importLocationPointOtherNames();
        importLocationPointCounties();
        importGeolCommodities();
        importGeolValueMat();
        importGeolAssocMat();
        importGeolMinStyle();
        importGeolHostLitho();
        importGeolAlteration();
        importGeolMinAge();
        importResourceRefs();
        importProdRefs();
    }
    
    /**
     * Import SITE table
     */
    private void importSites() throws Exception {
        System.out.println("Importing SITE...");
        
        String sql = "INSERT INTO SITE (Site_ID, Site_Name, Approx_Lat, Approx_Lon, " +
                    "MinReg_ID, Last_Updt, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Site.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 10) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, values[1]); // Site_ID *
                pstmt.setString(2, values[2]); // Site_Name
                pstmt.setObject(3, parseDecimal(values[9])); // Approx_Lat
                pstmt.setObject(4, parseDecimal(values[8])); // Approx_Lon
                pstmt.setString(5, cleanNull(values[6])); // MinReg_ID
                pstmt.setDate(6, parseDate(values[4])); // Last_Updt
                pstmt.setString(7, cleanNull(values[7])); // Remarks
                
                pstmt.executeUpdate();
                count++;
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " sites");
    }
    
    /**
     * Import REFERENCE table
     */
    private void importReferences() throws Exception {
        System.out.println("Importing REFERENCE...");
        
        String sql = "INSERT INTO REFERENCE (Ref_ID, Reference_Text, Last_Updt) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "References.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 3) {
                System.out.println("Row " + lineNum + " has only " + values.length + " columns");
                System.out.println("Line: " + line);
                continue;
            }
            
            try {
                pstmt.setString(1, values[1]); // Ref_ID
                pstmt.setString(2, values[2]); // Reference
                
                if (values.length > 3) {
                    pstmt.setDate(3, parseDate(values[3])); // Last_Updt
                } else {
                    pstmt.setDate(3, null);
                }
                
                pstmt.executeUpdate();
                count++;
            } catch (Exception e) {
                System.out.println("Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " references");
    }
    
    /**
     * Import LOCATION_POINT table
     */
    private void importLocationPoints() throws Exception {
        System.out.println("Importing LOCATION_POINT...");
        
        String sql = "INSERT INTO LOCATION_POINT (Site_ID, Ref_ID, Ftr_ID, Ftr_Name, " +
                    "Ftr_Type, Ftr_Group, Latitude, Longitude, Loc_Scale, Point_Definition, " +
                    "Poly_Definition, Loc_Date, Last_Updt, Remarks) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Loc_Pt.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 21) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                System.out.println("      Line: " + (line.length() > 100 ? line.substring(0, 100) + "..." : line));
                continue;
            }
            
            try {
                pstmt.setString(1, values[2]); // Site_ID *
                pstmt.setString(2, cleanNull(values[19])); // Ref_ID
                pstmt.setString(3, cleanSentinel(values[3])); // Ftr_ID *
                pstmt.setString(4, values[4]); // Ftr_Name
                pstmt.setString(5, cleanNull(values[8])); // Ftr_Type
                pstmt.setString(6, cleanNull(values[7])); // Ftr_Group
                pstmt.setDouble(7, Double.parseDouble(values[10])); // Lat_WGS84
                pstmt.setDouble(8, Double.parseDouble(values[11])); // Long_WGS84
                pstmt.setString(9, cleanNull(values[16])); // Loc_Scale
                pstmt.setString(10, cleanNull(values[12])); // Pt_Def
                pstmt.setString(11, cleanNull(values[13])); // Poly_Def
                pstmt.setObject(12, parseInt(values[17])); // Loc_Date
                pstmt.setDate(13, parseDate(values[6])); // Last_Updt
                pstmt.setString(14, cleanNull(values[20])); // Remarks
                
                pstmt.executeUpdate();
                count++;
                
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key constraint")) {
                    System.out.println("Error on row " + lineNum + ": Invalid foreign key - SKIPPING");
                } else {
                    System.out.println("Error on row " + lineNum + ": " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " location points");
    }
    
    /**
     * Import LOCATION_POLYGON table
     */
    private void importLocationPolygons() throws Exception {
        System.out.println("Importing LOCATION_POLYGON...");
        
        String sql = "INSERT INTO LOCATION_POLYGON (Site_ID, Ftr_ID, Ftr_Name, " +
                    "Area_SqKm, Area_Acres, Last_Updt, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Loc_Poly.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 9) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, values[2]); // Site_ID
                pstmt.setString(2, values[3]); // Ftr_ID
                pstmt.setString(3, values[4]); // Ftr_Name
                pstmt.setObject(4, parseDecimal(values[7])); // Area_SqKm
                pstmt.setObject(5, parseDecimal(values[8])); // Area_Acres
                pstmt.setDate(6, parseDate(values[5])); // Last_Updt
                pstmt.setString(7, cleanNull(values[6])); // Remarks
                
                pstmt.executeUpdate();
                count++;
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " location polygons");
    }
    
    /**
     * Import LOCATION_POLYGON_SW table
     */
    private void importLocationPolygonsSW() throws Exception {
        System.out.println("Importing LOCATION_POLYGON_SW...");
        
        String sql = "INSERT INTO LOCATION_POLYGON_SW (Ref_ID, Ftr_ID, Ftr_Name, State, " +
                    "County, Area_Acres, Loc_Date, Last_Updt, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Loc_Poly_Sw.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 11) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, cleanNull(values[9])); // Ref_ID
                pstmt.setString(2, values[2]); // Ftr_ID
                pstmt.setString(3, values[3]); // Ftr_Name
                pstmt.setString(4, cleanNull(values[5])); // State
                pstmt.setString(5, cleanNull(values[6])); // County
                pstmt.setObject(6, parseDecimal(values[12])); // Area_Acres
                pstmt.setObject(7, parseInt(values[7])); // Loc_Date
                pstmt.setDate(8, parseDate(values[4])); // Last_Updt
                pstmt.setString(9, cleanNull(values[10])); // Remarks
                
                pstmt.executeUpdate();
                count++;
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key constraint")) {
                    System.out.println("  WARNING: Row " + lineNum + " invalid foreign key - SKIPPING");
                } else {
                    System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " SW location polygons");
    }
    
    /**
     * Import GEOL_MIN_OCC table
     */
    private void importGeolMinOcc() throws Exception {
        System.out.println("Importing GEOL_MIN_OCC...");
        
        String sql = "INSERT INTO GEOL_MIN_OCC (Site_ID, Ref_ID, Ftr_ID, Ftr_Name, " +
                    "Ftr_Type, Host_Age, Host_Name, Last_Updt, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "GeolMinOcc.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 17) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, values[1]); // Site_ID *
                pstmt.setString(2, cleanNull(values[15])); // Ref_ID
                pstmt.setString(3, values[2]); // Ftr_ID *
                pstmt.setString(4, values[3]); // Ftr_Name
                pstmt.setString(5, cleanNull(values[5])); // Ftr_Type
                pstmt.setString(6, cleanNull(values[11])); // Host_Age
                pstmt.setString(7, cleanNull(values[12])); // Host_Name
                pstmt.setDate(8, parseDate(values[4])); // Last_Updt
                pstmt.setString(9, cleanNull(values[16])); // Remarks
                
                pstmt.executeUpdate();
                count++;
                
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key constraint")) {
                    System.out.println("  WARNING: Row " + lineNum + " invalid foreign key - SKIPPING");
                } else {
                    System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " geological mineral occurrences");
    }
    
    /**
     * Import RESOURCE table
     */
    private void importResources() throws Exception {
        System.out.println("Importing RESOURCE...");
        
        String sql = "INSERT INTO RESOURCE (Site_ID, Ftr_ID, Ftr_Name, Material, Resource_Date, " +
                    "Mat_Type, Mat_Amnt, Mat_Units, Grade, Grade_Unit, Cutoff_Grade, Cutoff_Unit, " +
                    "Contained_Amnt, Contained_Units, Resource_Class, Resource_Descr, Resource_Code, " +
                    "Ref_Detail, Last_Updt, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Resources.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 31) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, values[1]); // Site_ID *
                pstmt.setString(2, cleanSentinel(values[2])); // Ftr_ID * (NULLABLE)
                pstmt.setString(3, values[3]); // Ftr_Name
                pstmt.setString(4, values[5]); // Material
                pstmt.setObject(5, parseInt(values[6])); // Rsrc_Date
                pstmt.setString(6, cleanNull(values[7])); // Mat_Type
                pstmt.setObject(7, parseDecimal(values[8])); // Mat_Amnt
                pstmt.setString(8, cleanNull(values[9])); // Mat_Units
                pstmt.setObject(9, parseDecimal(values[10])); // Grade
                pstmt.setString(10, cleanNull(values[11])); // Grade_Unit
                pstmt.setObject(11, parseDecimal(values[12])); // CutOffGrad
                pstmt.setString(12, cleanNull(values[13])); // CutOffUnit
                pstmt.setObject(13, parseDecimal(values[14])); // Contained
                pstmt.setString(14, cleanNull(values[15])); // Cont_Units
                pstmt.setString(15, cleanNull(values[25])); // Rsrc_Class
                pstmt.setString(16, cleanNull(values[26])); // Rsrc_Descr
                pstmt.setString(17, cleanNull(values[27])); // Rsrc_Code
                pstmt.setString(18, cleanNull(values[28])); // Ref_Detail
                pstmt.setDate(19, parseDate(values[4])); // Last_Updt
                pstmt.setString(20, cleanNull(values[30])); // Remarks
                
                pstmt.executeUpdate();
                count++;
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " resource estimates");
    }
    
    /**
     * Import PRODUCTION table
     */
    private void importProduction() throws Exception {
        System.out.println("Importing PRODUCTION...");
        
        String sql = "INSERT INTO PRODUCTION (Site_ID, Ftr_ID, Ftr_Name, Assoc_Deposit, Material, " +
                    "Year_From, Year_To, Mat_Type, Mat_Amnt, Mat_Units, Grade, Grade_Unit, " +
                    "Cutoff_Grade, Cutoff_Unit, Contained_Amnt, Contained_Units, Recovery_Amnt, " +
                    "Recovery_Units, Prod_Value, Ref_Detail, Last_Updt, Remarks) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Production.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 35) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, values[1]); // Site_ID *
                pstmt.setString(2, values[2]); // Ftr_ID *
                pstmt.setString(3, values[3]); // Ftr_Name
                pstmt.setString(4, cleanNull(values[5])); // Assoc_Dep
                pstmt.setString(5, values[6]); // Material
                pstmt.setObject(6, parseInt(values[7])); // Year_From
                pstmt.setObject(7, parseInt(values[8])); // Year_To
                pstmt.setString(8, cleanNull(values[9])); // Mat_Type
                pstmt.setObject(9, parseDecimal(values[10])); // Mat_Amnt
                pstmt.setString(10, cleanNull(values[11])); // Mat_Units
                pstmt.setObject(11, parseDecimal(values[12])); // Grade
                pstmt.setString(12, cleanNull(values[13])); // Grade_Unit
                pstmt.setObject(13, parseDecimal(values[14])); // CutOffGrad
                pstmt.setString(14, cleanNull(values[15])); // CutOffUnit
                pstmt.setObject(15, parseDecimal(values[16])); // Contained
                pstmt.setString(16, cleanNull(values[17])); // Cont_Units
                pstmt.setObject(17, parseDecimal(values[18])); // Rcvry_Amt
                pstmt.setString(18, cleanNull(values[19])); // Rcvry_Unit
                pstmt.setObject(19, parseDecimal(values[20])); // Prod_USD
                pstmt.setString(20, cleanNull(values[32])); // Ref_Detail
                pstmt.setDate(21, parseDate(values[4])); // Last_Updt
                pstmt.setString(22, cleanNull(values[34])); // Remarks
                
                pstmt.executeUpdate();
                count++;
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " production records");
    }
    
    /**
     * Import HISTORY table
     */
    private void importHistory() throws Exception {
        System.out.println("Importing HISTORY...");
        
        String sql = "INSERT INTO HISTORY (Site_ID, Ref_ID, Ftr_ID, Ftr_Name, Status, " +
                    "Status_Detail, Year_From, Year_To, Last_Updt, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "History.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 12) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, values[1]); // Site_ID *
                pstmt.setString(2, cleanNull(values[10])); // Ref_ID
                pstmt.setString(3, cleanSentinel(values[2])); // Ftr_ID * (NULLABLE)
                pstmt.setString(4, values[3]); // Ftr_Name
                pstmt.setString(5, values[5]); // Status
                pstmt.setString(6, cleanNull(values[6])); // Status_Detail
                pstmt.setObject(7, parseInt(values[7])); // Year_From
                pstmt.setObject(8, parseInt(values[8])); // Year_To
                pstmt.setDate(9, parseDate(values[4])); // Last_Updt
                pstmt.setString(10, cleanNull(values[11])); // Remarks
                
                pstmt.executeUpdate();
                count++;
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key constraint")) {
                    System.out.println("  WARNING: Row " + lineNum + " invalid foreign key - SKIPPING");
                } else {
                    System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " history records");
    }
    
    /**
     * Import DEPOSIT_MODELS table
     */
    private void importDepositModels() throws Exception {
        System.out.println("Importing DEPOSIT_MODELS...");
        
        String sql = "INSERT INTO DEPOSIT_MODELS (Site_ID, Ftr_ID, Ftr_Name, Model_Num_Name, " +
                    "Model_Ref_ID, GEM_Name, GEM_Ref_ID, Last_Updt, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Dep_Model.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 11) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, values[1]); // Site_ID *
                pstmt.setString(2, values[2]); // Ftr_ID *
                pstmt.setString(3, values[3]); // Ftr_Name
                pstmt.setString(4, cleanNull(values[5])); // DpMd_NoNm
                pstmt.setString(5, cleanNull(values[7])); // DpMd_RefID
                pstmt.setString(6, cleanNull(values[8])); // GEM_Name
                pstmt.setString(7, cleanNull(values[9])); // GEM_RefID
                pstmt.setDate(8, parseDate(values[4])); // Last_Updt
                pstmt.setString(9, cleanNull(values[10])); // Remarks
                
                pstmt.executeUpdate();
                count++;
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " deposit models");
    }
    
    /**
     * Import DESCR_SUMMARY table
     */
    private void importDescrSummary() throws Exception {
        System.out.println("Importing DESCR_SUMMARY...");
        
        String sql = "INSERT INTO DESCR_SUMMARY (Site_ID, Ref_ID, Ftr_ID, Descr_Type, " +
                    "Description, Last_Updt, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connect.prepareStatement(sql);
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Descr_Sum.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        int lineNum = 1;
        
        while ((line = br.readLine()) != null) {
            lineNum++;
            String[] values = parseCSVLine(line);
            
            if (values.length < 8) {
                System.out.println("  WARNING: Row " + lineNum + " has only " + values.length + " columns - SKIPPING");
                continue;
            }
            
            try {
                pstmt.setString(1, values[1]); // Site_ID *
                pstmt.setString(2, cleanNull(values[6])); // Ref_ID
                pstmt.setString(3, values[2]); // Ftr_ID *
                pstmt.setString(4, cleanNull(values[4])); // Descr_Type
                pstmt.setString(5, cleanNull(values[5])); // Descr
                pstmt.setDate(6, parseDate(values[3])); // Last_Updt
                pstmt.setString(7, cleanNull(values[7])); // Remarks
                
                pstmt.executeUpdate();
                count++;
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key constraint")) {
                    System.out.println("  WARNING: Row " + lineNum + " invalid foreign key - SKIPPING");
                } else {
                    System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("  WARNING: Error on row " + lineNum + ": " + e.getMessage());
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " descriptive summaries");
    }
    
    /**
     * Import SITE_COMMODITY junction table
     */
    private void importSiteCommodities() throws Exception {
        System.out.println("Importing SITE_COMMODITY (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO SITE_COMMODITY (Site_ID, Commodity) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Site.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 6) continue;
            
            String siteId = values[1]; // Site_ID *
            String commodities = values[5]; // Commodity column
            
            if (commodities != null && !commodities.trim().isEmpty() && !commodities.equals("<Null>")) {
                String[] commodityList = commodities.split(";");
                
                for (String commodity : commodityList) {
                    commodity = commodity.trim();
                    if (!commodity.isEmpty()) {
                        try {
                            pstmt.setString(1, siteId);
                            pstmt.setString(2, commodity);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " site-commodity relationships");
    }
    
    /**
     * Import SITE_OTHER_NAMES junction table
     */
    private void importSiteOtherNames() throws Exception {
        System.out.println("Importing SITE_OTHER_NAMES (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO SITE_OTHER_NAMES (Site_ID, Other_Name) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Site.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 4) continue;
            
            String siteId = values[1]; // Site_ID *
            String otherNames = values[3]; // Other_Name column
            
            if (otherNames != null && !otherNames.trim().isEmpty() && !otherNames.equals("<Null>")) {
                String[] nameList = otherNames.split(";");
                
                for (String name : nameList) {
                    name = name.trim();
                    if (!name.isEmpty()) {
                        try {
                            pstmt.setString(1, siteId);
                            pstmt.setString(2, name);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " site other names");
    }
    
    /**
     * Import LOCATION_POINT_COMMODITY junction table
     */
    private void importLocationPointCommodities() throws Exception {
        System.out.println("Importing LOCATION_POINT_COMMODITY (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO LOCATION_POINT_COMMODITY (Loc_Point_ID, Commodity) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Loc_Point_ID FROM LOCATION_POINT ORDER BY Loc_Point_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Loc_Pt.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 10) continue;
            
            int locPointId = rs.getInt("Loc_Point_ID");
            String commodities = values[9]; // Commodity column
            
            if (commodities != null && !commodities.trim().isEmpty() && !commodities.equals("<Null>")) {
                String[] commodityList = commodities.split(";");
                
                for (String commodity : commodityList) {
                    commodity = commodity.trim();
                    if (!commodity.isEmpty()) {
                        try {
                            pstmt.setInt(1, locPointId);
                            pstmt.setString(2, commodity);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " location point commodities");
    }
    
    /**
     * Import LOCATION_POINT_OTHER_NAMES junction table
     */
    private void importLocationPointOtherNames() throws Exception {
        System.out.println("Importing LOCATION_POINT_OTHER_NAMES (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO LOCATION_POINT_OTHER_NAMES (Loc_Point_ID, Other_Name) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Loc_Point_ID FROM LOCATION_POINT ORDER BY Loc_Point_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Loc_Pt.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 6) continue;
            
            int locPointId = rs.getInt("Loc_Point_ID");
            String otherNames = values[5]; // Other_Name column
            
            if (otherNames != null && !otherNames.trim().isEmpty() && !otherNames.equals("<Null>")) {
                String[] nameList = otherNames.split(";");
                
                for (String name : nameList) {
                    name = name.trim();
                    if (!name.isEmpty()) {
                        try {
                            pstmt.setInt(1, locPointId);
                            pstmt.setString(2, name);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " location point other names");
    }
    
    /**
     * Import LOCATION_POINT_COUNTY junction table
     */
    private void importLocationPointCounties() throws Exception {
        System.out.println("Importing LOCATION_POINT_COUNTY (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO LOCATION_POINT_COUNTY (Loc_Point_ID, County) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Loc_Point_ID FROM LOCATION_POINT ORDER BY Loc_Point_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Loc_Pt.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 16) continue;
            
            int locPointId = rs.getInt("Loc_Point_ID");
            String counties = values[15]; // County column
            
            if (counties != null && !counties.trim().isEmpty() && !counties.equals("<Null>")) {
                String[] countyList = counties.split(";");
                
                for (String county : countyList) {
                    county = county.trim();
                    if (!county.isEmpty()) {
                        try {
                            pstmt.setInt(1, locPointId);
                            pstmt.setString(2, county);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " location point counties");
    }
    
    /**
     * Import GEOL_COMMODITY junction table
     */
    private void importGeolCommodities() throws Exception {
        System.out.println("Importing GEOL_COMMODITY (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO GEOL_COMMODITY (Geol_Min_ID, Commodity) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Geol_Min_ID FROM GEOL_MIN_OCC ORDER BY Geol_Min_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "GeolMinOcc.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 7) continue;
            
            int geolMinId = rs.getInt("Geol_Min_ID");
            String commodities = values[6]; // Commodity column
            
            if (commodities != null && !commodities.trim().isEmpty() && !commodities.equals("<Null>")) {
                String[] commodityList = commodities.split(";");
                
                for (String commodity : commodityList) {
                    commodity = commodity.trim();
                    if (!commodity.isEmpty()) {
                        try {
                            pstmt.setInt(1, geolMinId);
                            pstmt.setString(2, commodity);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " geological commodities");
    }
    
    /**
     * Import GEOL_VALUE_MAT junction table
     */
    private void importGeolValueMat() throws Exception {
        System.out.println("Importing GEOL_VALUE_MAT (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO GEOL_VALUE_MAT (Geol_Min_ID, Value_Material) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Geol_Min_ID FROM GEOL_MIN_OCC ORDER BY Geol_Min_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "GeolMinOcc.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 8) continue;
            
            int geolMinId = rs.getInt("Geol_Min_ID");
            String valueMats = values[7]; // Value_Mat column
            
            if (valueMats != null && !valueMats.trim().isEmpty() && !valueMats.equals("<Null>")) {
                String[] matList = valueMats.split(";");
                
                for (String mat : matList) {
                    mat = mat.trim();
                    if (!mat.isEmpty()) {
                        try {
                            pstmt.setInt(1, geolMinId);
                            pstmt.setString(2, mat);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " geological value materials");
    }
    
    /**
     * Import GEOL_ASSOC_MAT junction table
     */
    private void importGeolAssocMat() throws Exception {
        System.out.println("Importing GEOL_ASSOC_MAT (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO GEOL_ASSOC_MAT (Geol_Min_ID, Assoc_Material) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Geol_Min_ID FROM GEOL_MIN_OCC ORDER BY Geol_Min_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "GeolMinOcc.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 9) continue;
            
            int geolMinId = rs.getInt("Geol_Min_ID");
            String assocMats = values[8]; // Assoc_Mat column
            
            if (assocMats != null && !assocMats.trim().isEmpty() && !assocMats.equals("<Null>")) {
                String[] matList = assocMats.split(";");
                
                for (String mat : matList) {
                    mat = mat.trim();
                    if (!mat.isEmpty()) {
                        try {
                            pstmt.setInt(1, geolMinId);
                            pstmt.setString(2, mat);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " geological associated materials");
    }
    
    /**
     * Import GEOL_MIN_STYLE junction table
     */
    private void importGeolMinStyle() throws Exception {
        System.out.println("Importing GEOL_MIN_STYLE (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO GEOL_MIN_STYLE (Geol_Min_ID, Mineralization_Style) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Geol_Min_ID FROM GEOL_MIN_OCC ORDER BY Geol_Min_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "GeolMinOcc.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 10) continue;
            
            int geolMinId = rs.getInt("Geol_Min_ID");
            String minStyles = values[9]; // Min_Style column
            
            if (minStyles != null && !minStyles.trim().isEmpty() && !minStyles.equals("<Null>")) {
                String[] styleList = minStyles.split(";");
                
                for (String style : styleList) {
                    style = style.trim();
                    if (!style.isEmpty()) {
                        try {
                            pstmt.setInt(1, geolMinId);
                            pstmt.setString(2, style);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " mineralization styles");
    }
    
    /**
     * Import GEOL_HOST_LITHO junction table
     */
    private void importGeolHostLitho() throws Exception {
        System.out.println("Importing GEOL_HOST_LITHO (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO GEOL_HOST_LITHO (Geol_Min_ID, Host_Lithology) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Geol_Min_ID FROM GEOL_MIN_OCC ORDER BY Geol_Min_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "GeolMinOcc.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 14) continue;
            
            int geolMinId = rs.getInt("Geol_Min_ID");
            String hostLithos = values[13]; // Host_Litho column
            
            if (hostLithos != null && !hostLithos.trim().isEmpty() && !hostLithos.equals("<Null>")) {
                String[] lithoList = hostLithos.split(";");
                
                for (String litho : lithoList) {
                    litho = litho.trim();
                    if (!litho.isEmpty()) {
                        try {
                            pstmt.setInt(1, geolMinId);
                            pstmt.setString(2, litho);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " host lithologies");
    }
    
    /**
     * Import GEOL_ALTERATION junction table
     */
    private void importGeolAlteration() throws Exception {
        System.out.println("Importing GEOL_ALTERATION (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO GEOL_ALTERATION (Geol_Min_ID, Alteration_Type) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Geol_Min_ID FROM GEOL_MIN_OCC ORDER BY Geol_Min_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "GeolMinOcc.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 15) continue;
            
            int geolMinId = rs.getInt("Geol_Min_ID");
            String alterations = values[14]; // Alteration column
            
            if (alterations != null && !alterations.trim().isEmpty() && !alterations.equals("<Null>")) {
                String[] alterList = alterations.split(";");
                
                for (String alter : alterList) {
                    alter = alter.trim();
                    if (!alter.isEmpty()) {
                        try {
                            pstmt.setInt(1, geolMinId);
                            pstmt.setString(2, alter);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " alteration types");
    }
    
    /**
     * Import GEOL_MIN_AGE junction table
     */
    private void importGeolMinAge() throws Exception {
        System.out.println("Importing GEOL_MIN_AGE (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO GEOL_MIN_AGE (Geol_Min_ID, Mineralization_Age) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Geol_Min_ID FROM GEOL_MIN_OCC ORDER BY Geol_Min_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "GeolMinOcc.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 11) continue;
            
            int geolMinId = rs.getInt("Geol_Min_ID");
            String minAges = values[10]; // Min_Age column
            
            if (minAges != null && !minAges.trim().isEmpty() && !minAges.equals("<Null>")) {
                String[] ageList = minAges.split(";");
                
                for (String age : ageList) {
                    age = age.trim();
                    if (!age.isEmpty()) {
                        try {
                            pstmt.setInt(1, geolMinId);
                            pstmt.setString(2, age);
                            pstmt.executeUpdate();
                            count++;
                        } catch (Exception e) {
                            // Ignore duplicates
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " mineralization ages");
    }
    
    /**
     * Import RESOURCE_REFS junction table
     */
    private void importResourceRefs() throws Exception {
        System.out.println("Importing RESOURCE_REFS (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO RESOURCE_REFS (Resource_ID, Ref_ID) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Resource_ID FROM RESOURCE ORDER BY Resource_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Resources.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 30) continue;
            
            int resourceId = rs.getInt("Resource_ID");
            String refIds = values[29]; // Ref_ID column
            
            if (refIds != null && !refIds.trim().isEmpty() && !refIds.equals("<Null>")) {
                String[] refList = refIds.split(";");
                
                for (String refId : refList) {
                    refId = refId.trim();
                    if (!refId.isEmpty()) {
                        try {
                            pstmt.setInt(1, resourceId);
                            pstmt.setString(2, refId);
                            pstmt.executeUpdate();
                            count++;
                        } catch (SQLException e) {
                            // Skip if Ref_ID doesn't exist (orphaned reference)
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " resource references");
    }
    
    /**
     * Import PROD_REFS junction table
     */
    private void importProdRefs() throws Exception {
        System.out.println("Importing PROD_REFS (splitting multivalued)...");
        
        String sql = "INSERT IGNORE INTO PROD_REFS (Prod_ID, Ref_ID) VALUES (?, ?)";
        PreparedStatement pstmt = connect.prepareStatement(sql);
        
        ResultSet rs = statement.executeQuery("SELECT Prod_ID FROM PRODUCTION ORDER BY Prod_ID");
        
        BufferedReader br = new BufferedReader(new FileReader(CSV_PATH + "Production.csv"));
        String line;
        br.readLine(); // Skip header
        
        int count = 0;
        while ((line = br.readLine()) != null && rs.next()) {
            String[] values = parseCSVLine(line);
            
            if (values.length < 34) continue;
            
            int prodId = rs.getInt("Prod_ID");
            String refIds = values[33]; // Ref_ID column
            
            if (refIds != null && !refIds.trim().isEmpty() && !refIds.equals("<Null>")) {
                String[] refList = refIds.split(";");
                
                for (String refId : refList) {
                    refId = refId.trim();
                    if (!refId.isEmpty()) {
                        try {
                            pstmt.setInt(1, prodId);
                            pstmt.setString(2, refId);
                            pstmt.executeUpdate();
                            count++;
                        } catch (SQLException e) {
                            // Skip if Ref_ID doesn't exist
                        }
                    }
                }
            }
        }
        
        br.close();
        rs.close();
        pstmt.close();
        
        System.out.println("Imported " + count + " production references");
    }
    
    /**
     * Verify data was imported correctly
     */
    private void verifyImport() throws SQLException {
        System.out.println("VERIFICATION: ROW COUNTS");
        
        String[] tables = {
            "SITE", "REFERENCE", "LOCATION_POINT", "LOCATION_POLYGON", "LOCATION_POLYGON_SW",
            "GEOL_MIN_OCC", "RESOURCE", "PRODUCTION", "HISTORY", "DEPOSIT_MODELS", "DESCR_SUMMARY",
            "SITE_COMMODITY", "SITE_OTHER_NAMES", "LOCATION_POINT_COMMODITY", 
            "LOCATION_POINT_OTHER_NAMES", "LOCATION_POINT_COUNTY",
            "GEOL_COMMODITY", "GEOL_VALUE_MAT", "GEOL_ASSOC_MAT", "GEOL_MIN_STYLE",
            "GEOL_HOST_LITHO", "GEOL_ALTERATION", "GEOL_MIN_AGE",
            "RESOURCE_REFS", "PROD_REFS"
        };
        
        for (String table : tables) {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) as cnt FROM " + table);
            if (rs.next()) {
                System.out.printf("%-30s: %,6d rows\n", table, rs.getInt("cnt"));
            }
            rs.close();
        }
    }
    
    /**
     * Close database connection
     */
    private void closeConnection() {
        try {
            if (statement != null) statement.close();
            if (connect != null) connect.close();
            System.out.println("\nDatabase connection closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Parse CSV line handling quotes and commas
     */
    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        
        return values.toArray(new String[0]);
    }
    
    /**
     * Convert "<Null>" and empty strings to null
     */
    private String cleanNull(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("<Null>")) {
            return null;
        }
        return value.trim();
    }
    
    /**
     * Convert "-1111" sentinel value to null
     */
    private String cleanSentinel(String value) {
        if (value == null || value.trim().equals("-1111") || value.equals("<Null>")) {
            return null;
        }
        return value.trim();
    }
    
    /**
     * Parse string to Integer, return null if invalid
     */
    private Integer parseInt(String value) {
        try {
            if (value == null || value.trim().isEmpty() || value.equals("<Null>")) {
                return null;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parse string to Double, return null if invalid
     */
    private Double parseDecimal(String value) {
        try {
            if (value == null || value.trim().isEmpty() || value.equals("<Null>")) {
                return null;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parse string to SQL Date
     */
    private java.sql.Date parseDate(String value) {
        try {
            if (value == null || value.trim().isEmpty() || value.equals("<Null>")) {
                return null;
            }
            
            String cleaned = value.trim();
            
            // Handle M/d/yyyy format (e.g. "6/7/2018")
            if (cleaned.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] parts = cleaned.split("/");
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                return java.sql.Date.valueOf(String.format("%04d-%02d-%02d", year, month, day));
            }
            
            // Handle yyyy-MM-dd format
            return java.sql.Date.valueOf(cleaned);
            
        } catch (Exception e) {
            return null;
        }
    }
}