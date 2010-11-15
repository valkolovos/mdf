package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

public class DataValidationUtilTest {

    @Test()
    public void testValidateCapturedDataMockDataManagerStringStringString() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        assertFalse(DataValidationUtil.validateCapturedData(mdm, "TABLE_A", "COLUMN_1", "VALUE"));
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1) VALUES ( ? )");
        ps.setString(1, "VALUE");
        ps.executeUpdate();
        assertTrue(DataValidationUtil.validateCapturedData(mdm, "TABLE_A", "COLUMN_1", "VALUE"));
        assertFalse(DataValidationUtil.validateCapturedData(mdm, "TABLE_A", "COLUMN_1", "FOO"));
    }

    @Test()
    public void testValidateCapturedDataMockDataManagerStringMapOfStringString() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        Map<String, String> columnMap = new HashMap<String, String>();
        columnMap.put("COLUMN_1", "VALUE");
        columnMap.put("COLUMN_2", "VALUE");
        assertFalse(DataValidationUtil.validateCapturedData(mdm, "TABLE_A", columnMap));
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1, COLUMN_2) VALUES ( ?, ? )");
        ps.setString(1, "VALUE");
        ps.setString(2, "VALUE");
        ps.executeUpdate();
        assertTrue(DataValidationUtil.validateCapturedData(mdm, "TABLE_A", columnMap));
        columnMap.put("COLUMN_2", "FOO");
        assertFalse(DataValidationUtil.validateCapturedData(mdm, "TABLE_A", columnMap));
    }

    @Test()
    public void testGetCapturedDataMockDataManagerStringStringString() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1, COLUMN_2) VALUES ( ?, ? )");
        ps.setString(1, "VALUE");
        ps.setString(2, "VALUE 2");
        ps.executeUpdate();
        Map<String, String> data = DataValidationUtil.getCapturedData(mdm, "TABLE_A", "COLUMN_1", "VALUE");
        assertEquals(2, data.size());
        assertEquals("VALUE", data.get("COLUMN_1"));
        assertEquals("VALUE 2", data.get("COLUMN_2"));
    }

    @Test()
    public void testGetCapturedDataMockDataManagerStringMapOfStringString() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        Map<String, String> columnMap = new HashMap<String, String>();
        columnMap.put("COLUMN_1", "VALUE");
        columnMap.put("COLUMN_2", "VALUE 2");
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1, COLUMN_2, COLUMN_3) VALUES ( ?, ?, ? )");
        ps.setString(1, "VALUE");
        ps.setString(2, "VALUE 2");
        ps.setString(3, "VALUE 3");
        ps.executeUpdate();
        Map<String, String> data = DataValidationUtil.getCapturedData(mdm, "TABLE_A", columnMap);
        assertEquals(3, data.size());
        assertEquals("VALUE", data.get("COLUMN_1"));
        assertEquals("VALUE 2", data.get("COLUMN_2"));
        assertEquals("VALUE 3", data.get("COLUMN_3"));
    }

    @Test()
    public void testValidateCapturedDataStringStringString() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        DataValidationUtil util = new DataValidationUtil(mdm);
        assertFalse(util.validateCapturedData("TABLE_A", "COLUMN_1", "VALUE"));
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1) VALUES ( ? )");
        ps.setString(1, "VALUE");
        ps.executeUpdate();
        assertTrue(util.validateCapturedData("TABLE_A", "COLUMN_1", "VALUE"));
        assertFalse(util.validateCapturedData("TABLE_A", "COLUMN_1", "FOO"));
    }

    @Test()
    public void testValidateCapturedDataStringMapOfStringString() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        DataValidationUtil util = new DataValidationUtil(mdm);
        Map<String, String> columnMap = new HashMap<String, String>();
        columnMap.put("COLUMN_1", "VALUE");
        columnMap.put("COLUMN_2", "VALUE");
        assertFalse(util.validateCapturedData("TABLE_A", columnMap));
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1, COLUMN_2) VALUES ( ?, ? )");
        ps.setString(1, "VALUE");
        ps.setString(2, "VALUE");
        ps.executeUpdate();
        assertTrue(util.validateCapturedData("TABLE_A", columnMap));
        columnMap.put("COLUMN_2", "FOO");
        assertFalse(util.validateCapturedData("TABLE_A", columnMap));
    }

    @Test()
    public void testGetCapturedDataStringStringString() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        DataValidationUtil util = new DataValidationUtil(mdm);
        assertEquals(0, util.getCapturedData("TABLE_A", "COLUMN_1", "VALUE").size());
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1, COLUMN_2) VALUES ( ?, ? )");
        ps.setString(1, "VALUE");
        ps.setString(2, "VALUE 2");
        ps.executeUpdate();
        Map<String, String> data = util.getCapturedData("TABLE_A", "COLUMN_1", "VALUE");
        assertEquals(2, data.size());
        assertEquals("VALUE", data.get("COLUMN_1"));
        assertEquals("VALUE 2", data.get("COLUMN_2"));
    }

    @Test()
    public void testGetCapturedDataStringMapOfStringString() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        DataValidationUtil util = new DataValidationUtil(mdm);
        Map<String, String> columnMap = new HashMap<String, String>();
        columnMap.put("COLUMN_1", "VALUE");
        columnMap.put("COLUMN_2", "VALUE 2");
        assertEquals(0, util.getCapturedData("TABLE_A", columnMap).size());
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1, COLUMN_2, COLUMN_3) VALUES ( ?, ?, ? )");
        ps.setString(1, "VALUE");
        ps.setString(2, "VALUE 2");
        ps.setString(3, "VALUE 3");
        ps.executeUpdate();
        Map<String, String> data = util.getCapturedData("TABLE_A", columnMap);
        assertEquals(3, data.size());
        assertEquals("VALUE", data.get("COLUMN_1"));
        assertEquals("VALUE 2", data.get("COLUMN_2"));
        assertEquals("VALUE 3", data.get("COLUMN_3"));
    }

    @Test()
    public void testMultipleInserts() throws Exception {
        MockDataManager mdm = new MockDataManager();
        MockDataSource mds = new MockDataSource(mdm);
        DataValidationUtil util = new DataValidationUtil(mdm);
        Map<String, String> columnMap = new HashMap<String, String>();
        columnMap.put("COLUMN_1", "VALUE");
        columnMap.put("COLUMN_2", "VALUE 2");
        assertEquals(0, util.getCapturedData("TABLE_A", columnMap).size());
        Connection c = mds.getConnection();
        PreparedStatement ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1, COLUMN_2, COLUMN_3) VALUES ( ?, ?, ? )");
        ps.setString(1, "VALUE");
        ps.setString(2, "VALUE 2");
        ps.setString(3, "VALUE 3");
        ps.executeUpdate();
        ps = c.prepareStatement("INSERT INTO TABLE_A (COLUMN_1, COLUMN_2, COLUMN_3) VALUES ( ?, ?, ? )");
        ps.setString(1, "VALUE");
        ps.setString(2, "VALUE 2");
        ps.setString(3, "VALUE 4");
        Map<String, String> data = util.getCapturedData("TABLE_A", columnMap);
        assertEquals(3, data.size());
        assertEquals("VALUE", data.get("COLUMN_1"));
        assertEquals("VALUE 2", data.get("COLUMN_2"));
        assertEquals("VALUE 3", data.get("COLUMN_3"));
    }

}
