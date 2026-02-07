CREATE DATABASE cobalt_mining;
USE cobalt_mining;

CREATE TABLE SITE (
    Site_ID VARCHAR(10) PRIMARY KEY,
    Site_Name VARCHAR(100) NOT NULL,
    Approx_Lat DECIMAL(10, 6),
    Approx_Lon DECIMAL(11, 6),
    MinReg_ID VARCHAR(50),
    Last_Updt DATE,
    Remarks TEXT
) ENGINE=InnoDB;

CREATE TABLE REFERENCE (
    Ref_ID VARCHAR(50) PRIMARY KEY,
    Reference_Text TEXT NOT NULL,
    Last_Updt DATE
) ENGINE=InnoDB;

CREATE TABLE LOCATION_POINT (
    Loc_Point_ID INT AUTO_INCREMENT PRIMARY KEY,
    Site_ID VARCHAR(10) NOT NULL,
    Ref_ID VARCHAR(50),
    Ftr_ID VARCHAR(20), 
    Ftr_Name VARCHAR(150),
    Ftr_Type VARCHAR(50),
    Ftr_Group VARCHAR(50),
    Latitude DECIMAL(10, 6) NOT NULL,
    Longitude DECIMAL(11, 6) NOT NULL,
    Loc_Scale VARCHAR(50),
    Point_Definition TEXT,
    Poly_Definition TEXT,
    Loc_Date INT,
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_locpt_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE,
    CONSTRAINT fk_locpt_ref FOREIGN KEY (Ref_ID) 
        REFERENCES REFERENCE(Ref_ID) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE LOCATION_POLYGON (
    Loc_Poly_ID INT AUTO_INCREMENT PRIMARY KEY,
    Site_ID VARCHAR(10) NOT NULL,
    Ftr_ID VARCHAR(20),
    Ftr_Name VARCHAR(150),
    Area_SqKm DECIMAL(12, 4),
    Area_Acres DECIMAL(12, 4),
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_locpoly_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE LOCATION_POLYGON_SW (
    Loc_Poly_SW_ID INT AUTO_INCREMENT PRIMARY KEY,
    Ref_ID VARCHAR(50),
    Ftr_ID VARCHAR(20),
    Ftr_Name VARCHAR(150),
    State VARCHAR(50),
    County VARCHAR(100),
    Area_Acres DECIMAL(12, 4),
    Loc_Date INT,
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_locpolysw_ref FOREIGN KEY (Ref_ID) 
        REFERENCES REFERENCE(Ref_ID) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE GEOL_MIN_OCC (
    Geol_Min_ID INT AUTO_INCREMENT PRIMARY KEY,
    Site_ID VARCHAR(10) NOT NULL,
    Ref_ID VARCHAR(50),
    Ftr_ID VARCHAR(20),
    Ftr_Name VARCHAR(150),
    Ftr_Type VARCHAR(50),
    Host_Age VARCHAR(100),
    Host_Name VARCHAR(150),
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_geolmin_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE,
    CONSTRAINT fk_geolmin_ref FOREIGN KEY (Ref_ID) 
        REFERENCES REFERENCE(Ref_ID) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE RESOURCE (
    Resource_ID INT AUTO_INCREMENT PRIMARY KEY,
    Site_ID VARCHAR(10) NOT NULL,
    Ftr_ID VARCHAR(20),  -- NULLABLE: 9 records with -1111 converted to NULL
    Ftr_Name VARCHAR(150),
    Material VARCHAR(50) NOT NULL,
    Resource_Date INT,
    Mat_Type VARCHAR(50),
    Mat_Amnt DECIMAL(15, 4),
    Mat_Units VARCHAR(50),
    Grade DECIMAL(10, 6),
    Grade_Unit VARCHAR(50),
    Cutoff_Grade DECIMAL(10, 6),
    Cutoff_Unit VARCHAR(50),
    Contained_Amnt DECIMAL(15, 4),
    Contained_Units VARCHAR(50),
    Resource_Class VARCHAR(50),
    Resource_Descr VARCHAR(200),
    Resource_Code VARCHAR(50),
    Ref_Detail VARCHAR(200),
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_resource_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE PRODUCTION (
    Prod_ID INT AUTO_INCREMENT PRIMARY KEY,
    Site_ID VARCHAR(10) NOT NULL,
    Ftr_ID VARCHAR(20),
    Ftr_Name VARCHAR(150),
    Assoc_Deposit VARCHAR(100),
    Material VARCHAR(50) NOT NULL,
    Year_From INT,
    Year_To INT,
    Mat_Type VARCHAR(50),
    Mat_Amnt DECIMAL(15, 4),
    Mat_Units VARCHAR(50),
    Grade DECIMAL(10, 6),
    Grade_Unit VARCHAR(50),
    Cutoff_Grade DECIMAL(10, 6),
    Cutoff_Unit VARCHAR(50),
    Contained_Amnt DECIMAL(15, 4),
    Contained_Units VARCHAR(50),
    Recovery_Amnt DECIMAL(15, 4),
    Recovery_Units VARCHAR(50),
    Prod_Value DECIMAL(15, 2),
    Ref_Detail VARCHAR(200),
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_production_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE HISTORY (
    History_ID INT AUTO_INCREMENT PRIMARY KEY,
    Site_ID VARCHAR(10) NOT NULL,
    Ref_ID VARCHAR(50),
    Ftr_ID VARCHAR(20),  -- NULLABLE: 19 records with -1111 converted to NULL
    Ftr_Name VARCHAR(150),
    Status VARCHAR(50) NOT NULL,
    Status_Detail TEXT,
    Year_From INT,
    Year_To INT,
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_history_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE,
    CONSTRAINT fk_history_ref FOREIGN KEY (Ref_ID) 
        REFERENCES REFERENCE(Ref_ID) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE DEPOSIT_MODELS (
    Dep_Model_ID INT AUTO_INCREMENT PRIMARY KEY,
    Site_ID VARCHAR(10) NOT NULL,
    Ftr_ID VARCHAR(20),
    Ftr_Name VARCHAR(150),
    Model_Num_Name VARCHAR(100),
    Model_Ref_ID VARCHAR(50),
    GEM_Name VARCHAR(100),
    GEM_Ref_ID VARCHAR(50),
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_depmodel_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE DESCR_SUMMARY (
    Descr_ID INT AUTO_INCREMENT PRIMARY KEY,
    Site_ID VARCHAR(10) NOT NULL,
    Ref_ID VARCHAR(50),
    Ftr_ID VARCHAR(20),
    Descr_Type VARCHAR(50),
    Description TEXT,
    Last_Updt DATE,
    Remarks TEXT,
    
    CONSTRAINT fk_descr_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE,
    CONSTRAINT fk_descr_ref FOREIGN KEY (Ref_ID) 
        REFERENCES REFERENCE(Ref_ID) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE SITE_COMMODITY (
    Site_ID VARCHAR(10),
    Commodity VARCHAR(50),
    
    PRIMARY KEY (Site_ID, Commodity),
    CONSTRAINT fk_sitecommodity_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE SITE_OTHER_NAMES (
    Site_ID VARCHAR(10),
    Other_Name VARCHAR(150),
    
    PRIMARY KEY (Site_ID, Other_Name),
    CONSTRAINT fk_siteothername_site FOREIGN KEY (Site_ID) 
        REFERENCES SITE(Site_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE LOCATION_POINT_COMMODITY (
    Loc_Point_ID INT,
    Commodity VARCHAR(50),
    
    PRIMARY KEY (Loc_Point_ID, Commodity),
    CONSTRAINT fk_locptcommodity_locpt FOREIGN KEY (Loc_Point_ID) 
        REFERENCES LOCATION_POINT(Loc_Point_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE LOCATION_POINT_OTHER_NAMES (
    Loc_Point_ID INT,
    Other_Name VARCHAR(150),
    
    PRIMARY KEY (Loc_Point_ID, Other_Name),
    CONSTRAINT fk_locptothername_locpt FOREIGN KEY (Loc_Point_ID) 
        REFERENCES LOCATION_POINT(Loc_Point_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE LOCATION_POINT_COUNTY (
    Loc_Point_ID INT,
    County VARCHAR(100),
    
    PRIMARY KEY (Loc_Point_ID, County),
    CONSTRAINT fk_locptcounty_locpt FOREIGN KEY (Loc_Point_ID) 
        REFERENCES LOCATION_POINT(Loc_Point_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE GEOL_COMMODITY (
    Geol_Min_ID INT,
    Commodity VARCHAR(50),
    
    PRIMARY KEY (Geol_Min_ID, Commodity),
    CONSTRAINT fk_geolcommodity_geol FOREIGN KEY (Geol_Min_ID) 
        REFERENCES GEOL_MIN_OCC(Geol_Min_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE GEOL_VALUE_MAT (
    Geol_Min_ID INT,
    Value_Material VARCHAR(100),
    
    PRIMARY KEY (Geol_Min_ID, Value_Material),
    CONSTRAINT fk_geolvaluemat_geol FOREIGN KEY (Geol_Min_ID) 
        REFERENCES GEOL_MIN_OCC(Geol_Min_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE GEOL_ASSOC_MAT (
    Geol_Min_ID INT,
    Assoc_Material VARCHAR(100),
    
    PRIMARY KEY (Geol_Min_ID, Assoc_Material),
    CONSTRAINT fk_geolassocmat_geol FOREIGN KEY (Geol_Min_ID) 
        REFERENCES GEOL_MIN_OCC(Geol_Min_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE GEOL_MIN_STYLE (
    Geol_Min_ID INT,
    Mineralization_Style VARCHAR(100),
    
    PRIMARY KEY (Geol_Min_ID, Mineralization_Style),
    CONSTRAINT fk_geolminstyle_geol FOREIGN KEY (Geol_Min_ID) 
        REFERENCES GEOL_MIN_OCC(Geol_Min_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE GEOL_HOST_LITHO (
    Geol_Min_ID INT,
    Host_Lithology VARCHAR(100),
    
    PRIMARY KEY (Geol_Min_ID, Host_Lithology),
    CONSTRAINT fk_geolhostlitho_geol FOREIGN KEY (Geol_Min_ID) 
        REFERENCES GEOL_MIN_OCC(Geol_Min_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE GEOL_ALTERATION (
    Geol_Min_ID INT,
    Alteration_Type VARCHAR(100),
    
    PRIMARY KEY (Geol_Min_ID, Alteration_Type),
    CONSTRAINT fk_geolalteration_geol FOREIGN KEY (Geol_Min_ID) 
        REFERENCES GEOL_MIN_OCC(Geol_Min_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE GEOL_MIN_AGE (
    Geol_Min_ID INT,
    Mineralization_Age VARCHAR(100),
    
    PRIMARY KEY (Geol_Min_ID, Mineralization_Age),
    CONSTRAINT fk_geolminage_geol FOREIGN KEY (Geol_Min_ID) 
        REFERENCES GEOL_MIN_OCC(Geol_Min_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE RESOURCE_REFS (
    Resource_ID INT,
    Ref_ID VARCHAR(50),
    
    PRIMARY KEY (Resource_ID, Ref_ID),
    CONSTRAINT fk_resourcerefs_resource FOREIGN KEY (Resource_ID) 
        REFERENCES RESOURCE(Resource_ID) ON DELETE CASCADE,
    CONSTRAINT fk_resourcerefs_ref FOREIGN KEY (Ref_ID) 
        REFERENCES REFERENCE(Ref_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE PROD_REFS (
    Prod_ID INT,
    Ref_ID VARCHAR(50),
    
    PRIMARY KEY (Prod_ID, Ref_ID),
    CONSTRAINT fk_prodrefs_production FOREIGN KEY (Prod_ID) 
        REFERENCES PRODUCTION(Prod_ID) ON DELETE CASCADE,
    CONSTRAINT fk_prodrefs_ref FOREIGN KEY (Ref_ID) 
        REFERENCES REFERENCE(Ref_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

SHOW TABLES;