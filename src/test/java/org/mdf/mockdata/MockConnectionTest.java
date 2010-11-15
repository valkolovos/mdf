package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.testng.annotations.Test;

public class MockConnectionTest {
    
    @Test()
    public void testNativeSqlValidateSql() throws Exception {
        MockConnection connection = new MockConnection(new MockDataManager());
        String query = "validateCapturedData:TABLE_1|KEY1|VALUE|KEY2";
        String isValid = connection.nativeSQL(query);
        assertEquals("false", isValid);
        connection.prepareStatement("INSERT INTO TABLE_1 (KEY1) VALUES ('VALUE')").execute();
        query = "validateCapturedData:TABLE_1|KEY1|VALUE";
        isValid = connection.nativeSQL(query);
        assertEquals("true", isValid);
    }
    
    @Test()
    public void testNativeSqlSetMockData() throws Exception {
        MockConnection connection = new MockConnection(new MockDataManager());
        String query = "setMockData:" +
        		"<mock-data>" +
        		"  <category>" +
        		"    <test>" +
        		"      <request>" +
        		"        <param name=\"1\" value=\"foo\"/>" +
        		"      </request>" +
        		"      <response>" +
        		"        <param name=\"row\">" +
        		"            <param name=\"id\" value=\"foo\"/>" +
        		"        </param>" +
        		"      </response>" +
        		"    </test>" +
        		"  </category>" +
        		"</mock-data>";
        connection.nativeSQL(query);
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM TAB WHERE id = ?");
        ps.setString(1, "foo");
        ResultSet rs = ps.executeQuery();
        assertTrue(rs.next());
        assertEquals("foo", rs.getString("id"));
        
    }
    
    @Test()
    public void testNativeSqlAddCustomFunctionWithMockDriver() throws Exception {
        MockConnection connection = new MockConnection(new MockDataManager());
        String query = "setMockData:" +
        "<mock-data>" +
        "  <category>" +
        "    <test>" +
        "      <request>" +
        "        <param name=\"1\" value=\"foo\"/>" +
        "      </request>" +
        "      <response>" +
        "        <param name=\"row\">" +
        "            <param name=\"omh_reservation_id\" value=\"foo\"/>" +
        "            <param name=\"decoded_succ_numm\" value=\"1111222233334444\"/>" +
        "        </param>" +
        "      </response>" +
        "    </test>" +
        "  </category>" +
        "</mock-data>";
        connection.nativeSQL(query);

        query = "addCustomFunction:omh.decode_cc|2";
        connection.nativeSQL(query);

        connection.nativeSQL(query);
        String sql = "select OMH_RESERVATION_ID, OMH.DECODE_CC(SUCC_NUM, SUCC_POOL_ID) as DECODED_SUCC_NUM "
                + "from SUCC where SUCC_ID =" + " (select max(SUCC_ID) from SUCC where OMH_RESERVATION_ID = ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "foo");
        ResultSet rs = ps.executeQuery();
        assertTrue(rs.next());
        assertEquals("foo", rs.getString("omh_reservation_id"));
    }

    @Test()
    public void testNativeSqlAddCustomFunction() throws Exception {
        MockConnection connection = (MockConnection) MockDriver.getInstance().connect("jdbc:oracle:", null);
        String query = "setMockData:" +
        "<mock-data>" +
        "  <category>" +
        "    <test>" +
        "      <request>" +
        "        <param name=\"1\" value=\"foo\"/>" +
        "      </request>" +
        "      <response>" +
        "        <param name=\"row\">" +
        "            <param name=\"omh_reservation_id\" value=\"foo\"/>" +
        "            <param name=\"decoded_succ_numm\" value=\"1111222233334444\"/>" +
        "        </param>" +
        "      </response>" +
        "    </test>" +
        "  </category>" +
        "</mock-data>";
        connection.nativeSQL(query);

        query = "addCustomFunction:omh.decode_cc|2";
        connection.nativeSQL(query);

        connection.nativeSQL(query);
        String sql = "select OMH_RESERVATION_ID, OMH.DECODE_CC(SUCC_NUM, SUCC_POOL_ID) as DECODED_SUCC_NUM "
                + "from SUCC where SUCC_ID =" + " (select max(SUCC_ID) from SUCC where OMH_RESERVATION_ID = ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "foo");
        ResultSet rs = ps.executeQuery();
        assertTrue(rs.next());
        assertEquals("foo", rs.getString("omh_reservation_id"));
    }
}
