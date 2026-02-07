USE cobalt_mining;

-- Drop procedures if they already exist (for re-running this script)
DROP PROCEDURE IF EXISTS filter_geol_by_commodity_type;
DROP PROCEDURE IF EXISTS clean_resource_indicators;


-- PROCEDURE 1: Filter Geological Mineral Occurrences
-- Purpose: Filter GeolMinOcc by commodity and feature type
-- Inputs: 
--   - p_commodity: Commodity to search for (e.g., 'cobalt', 'copper', 'nickel')
--   - p_ftr_type: Feature type to search for (e.g., 'deposit', 'prospect')
-- Output: Table with Site_ID, Ftr_name, commodity, ftr_type, last_updated, value_mat

DELIMITER $$

CREATE PROCEDURE filter_geol_by_commodity_type(
    IN p_commodity VARCHAR(50),
    IN p_ftr_type VARCHAR(50)
)
BEGIN
    SELECT DISTINCT
        g.Site_ID,
        g.Ftr_Name,
        gc.Commodity,
        g.Ftr_Type,
        g.Last_Updt AS last_updated,
        gv.Value_Material AS value_mat
    FROM 
        GEOL_MIN_OCC g
    INNER JOIN 
        GEOL_COMMODITY gc ON g.Geol_Min_ID = gc.Geol_Min_ID
    LEFT JOIN 
        GEOL_VALUE_MAT gv ON g.Geol_Min_ID = gv.Geol_Min_ID
    WHERE 
        gc.Commodity = p_commodity
        AND g.Ftr_Type = p_ftr_type
    ORDER BY 
        g.Site_ID, g.Ftr_Name;
END$$

DELIMITER ;

-- PROCEDURE 2: Clean USGS Indicator Values from Resources
-- Purpose: Remove .111 indicator suffix from contained amounts
-- Inputs: None (processes all resources)
-- Output: Table with site_id, ftr_id, rsrc_date, contained (cleaned), cont_units

DELIMITER $$

CREATE PROCEDURE clean_resource_indicators()
BEGIN
    SELECT 
        Site_ID AS site_id,
        Ftr_ID AS ftr_id,
        Resource_Date AS rsrc_date,
        -- Clean by checking if decimal representation ends with 111
        CASE 
            WHEN Contained_Amnt IS NOT NULL THEN
                CASE
                    -- Check if ends with .1110 (most common: 480000.1110 → 480000)
                    WHEN (Contained_Amnt * 10000) MOD 10000 = 1110 THEN
                        CAST(FLOOR(Contained_Amnt) AS DECIMAL(20,4))
                    
                    -- Check if ends with .X111 pattern (like 1.3111 → 1.3, 68.3111 → 68.3)
                    WHEN (Contained_Amnt * 10000) MOD 1000 = 111 THEN
                        CAST(FLOOR(Contained_Amnt * 10) / 10 AS DECIMAL(20,4))
                    
                    -- Check if ends with .XX111 pattern (like 0.05111 → 0.05)
                    WHEN (Contained_Amnt * 100000) MOD 1000 = 111 THEN
                        CAST(FLOOR(Contained_Amnt * 100) / 100 AS DECIMAL(20,4))
                    
                    ELSE Contained_Amnt
                END
            ELSE NULL
        END AS contained,
        Contained_Units AS cont_units
    FROM 
        RESOURCE
    ORDER BY 
        Site_ID, Resource_Date;
END$$

DELIMITER ;

-- Test: Filter for cobalt deposits
CALL filter_geol_by_commodity_type('cobalt', 'Deposit');

-- Test: Filter for copper prospects
CALL filter_geol_by_commodity_type('copper', 'Prospect');

-- Test: Clean indicator values
CALL clean_resource_indicators();

-- Test: Count how many records match cobalt + Deposit
SELECT COUNT(*) AS total_cobalt_deposits
FROM GEOL_MIN_OCC g
INNER JOIN GEOL_COMMODITY gc ON g.Geol_Min_ID = gc.Geol_Min_ID
WHERE gc.Commodity = 'cobalt' AND g.Ftr_Type = 'Deposit';

-- Test: Check which resources have indicator values (.111 suffix)
SELECT Site_ID, Contained_Amnt, 
       CASE 
           WHEN CAST(Contained_Amnt AS CHAR) LIKE '%.111' THEN 'Has Indicator'
           ELSE 'No Indicator'
       END AS indicator_status
FROM RESOURCE
WHERE Contained_Amnt IS NOT NULL
LIMIT 20;
