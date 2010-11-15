package org.mdf.mockdata.capture;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.testng.annotations.Test;

import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.jdbc.StatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockDataSource;
import com.mockrunner.mock.jdbc.MockResultSet;

public class CaptureDataSourceTest {

    @Test()
    public void testExecutePreparedStatement() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        PreparedStatementResultSetHandler resultSetHandler = connection.getPreparedStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("id");
        resultSet.addColumn("description");
        resultSet.addRow(new Object[] { 1, "Description" });
        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection();
        c.setAutoCommit(true);
        PreparedStatement ps = c.prepareStatement("SELECT * FROM FOO_TABLE");
        ResultSet rs = ps.executeQuery();
        rs.next();
        assertFalse(rs.next());
        rs.close();
        MockData testData = captureDataSource.getTestData();
        assertNotNull(testData);
        assertEquals(1, testData.getCategoryCount());
        assertEquals(1, testData.getCategory(0).getParamCount());
        assertEquals("SELECT * FROM FOO_TABLE", testData.getCategory(0).getParam(0).getValue());
        assertEquals(1, testData.getCategory(0).getTestCount());
        assertEquals(0, testData.getCategory(0).getTest(0).getRequest().getParamCount());
        assertEquals(1, testData.getCategory(0).getTest(0).getResponse().getParamCount());
        Param rowParam = testData.getCategory(0).getTest(0).getResponse().getParam(0);
        assertEquals(2, rowParam.getParamCount());
        assertEquals("id", rowParam.getParam(0).getName());
        assertEquals("1", rowParam.getParam(0).getValue());
        assertEquals("description", rowParam.getParam(1).getName());
        assertEquals("Description", rowParam.getParam(1).getValue());
    }

    @Test()
    public void testAliases() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        PreparedStatementResultSetHandler resultSetHandler = connection.getPreparedStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("id1_1");
        resultSet.addColumn("desc1_2");
        resultSet.addRow(new Object[] { 1, "Description" });
        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection();
        c.setAutoCommit(true);
        String sql = "SELECT this_.id as id1_1, this_.description as desc1_2 FROM FOO_TABLE this_";
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        rs.next();
        assertFalse(rs.next());
        rs.close();
        MockData testData = captureDataSource.getTestData();
        assertNotNull(testData);
        assertEquals(1, testData.getCategoryCount());
        assertEquals(1, testData.getCategory(0).getParamCount());
        assertEquals(sql, testData.getCategory(0).getParam(0).getValue());
        assertEquals(1, testData.getCategory(0).getTestCount());
        assertEquals(0, testData.getCategory(0).getTest(0).getRequest().getParamCount());
        assertEquals(1, testData.getCategory(0).getTest(0).getResponse().getParamCount());
        Param rowParam = testData.getCategory(0).getTest(0).getResponse().getParam(0);
        assertEquals(2, rowParam.getParamCount());
        assertEquals("foo_table.id", rowParam.getParam(0).getName());
        assertEquals("1", rowParam.getParam(0).getValue());
        assertEquals("foo_table.description", rowParam.getParam(1).getName());
        assertEquals("Description", rowParam.getParam(1).getValue());
    }

    @Test()
    public void testMoreComplicatedAliases() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        PreparedStatementResultSetHandler resultSetHandler = connection.getPreparedStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("customer1_0_3_");
        resultSet.addColumn("anonymous2_0_3_");
        resultSet.addColumn("customer1_6_0_");
        resultSet.addColumn("customer1_6_1_");
        resultSet.addColumn("customer8_5_");
        resultSet.addColumn("customer8_6_");
        resultSet.addRow(new Object[] { 1, "Anonymous Cookie", 42, 42, 43, null });
        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection();
        c.setAutoCommit(true);
        String sql = "select this_.customer_member_id as customer1_0_3_, this_.anonymous_cookie_value as anonymous2_0_3_, "
                + "customertr1_.customer_traveler_profile_id as customer1_6_0_, "
                + "customertr1_.customer_traveler_profile_id as customer1_6_1_, "
                + "customerlo4_.customer_traveler_profile_id as customer8_5_, "
                + "customertr5_.customer_traveler_profile_id as customer8_6_ "
                + ""
                + "from customer_member this_, "
                + "customer_traveler_profile customertr1_, "
                + "customer_loyalty_card_profile customerlo4_, "
                + "customer_traveler_preference customertr5_ "
                + ""
                + "where this_.customer_member_id=customertr1_.customer_member_id "
                + "and customertr1_.customer_traveler_profile_id=customerlo4_.customer_traveler_profile_id(+) "
                + "and customertr1_.customer_traveler_profile_id=customertr5_.customer_traveler_profile_id(+) "
                + "and ?=this_.ref_point_of_sale_id "
                + "and ?=customertr1_.pii_phone "
                + "and customerlo4_.customer_traveler_profile_id = ?";
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        rs.next();
        assertFalse(rs.next());
        rs.close();
        MockData testData = captureDataSource.getTestData();
        assertNotNull(testData);
        assertEquals(1, testData.getCategoryCount());
        assertEquals(1, testData.getCategory(0).getParamCount());
        assertEquals(sql, testData.getCategory(0).getParam(0).getValue());
        assertEquals(1, testData.getCategory(0).getTestCount());
        assertEquals(0, testData.getCategory(0).getTest(0).getRequest().getParamCount());
        assertEquals(1, testData.getCategory(0).getTest(0).getResponse().getParamCount());
        Param rowParam = testData.getCategory(0).getTest(0).getResponse().getParam(0);
        assertEquals(5, rowParam.getParamCount());
        assertEquals("customer_member.customer_member_id", rowParam.getParam(0).getName());
        assertEquals("customer_member.anonymous_cookie_value", rowParam.getParam(1).getName());
        assertEquals("customer_traveler_profile.customer_traveler_profile_id", rowParam.getParam(2).getName());
        assertEquals("customer_loyalty_card_profile.customer_traveler_profile_id", rowParam.getParam(3).getName());
        assertEquals("customer_traveler_preference.customer_traveler_profile_id", rowParam.getParam(4).getName());
        assertEquals("1", rowParam.getParam(0).getValue());
        assertEquals("Anonymous Cookie", rowParam.getParam(1).getValue());
    }

    @Test()
    public void testExecutePreparedStatementWithParams() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        PreparedStatementResultSetHandler resultSetHandler = connection.getPreparedStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("id");
        resultSet.addColumn("description");
        resultSet.addRow(new Object[] { 1, "Description" });
        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection();
        PreparedStatement ps = c.prepareStatement("SELECT * FROM FOO_TABLE WHERE id = ?");
        ps.setInt(1, 1);
        ResultSet rs = ps.executeQuery();
        rs.next();
        MockData testData = captureDataSource.getTestData();
        assertEquals("SELECT * FROM FOO_TABLE WHERE id = ?", testData.getCategory(0).getParam(0).getValue());
        assertEquals(1, testData.getCategory(0).getTest(0).getRequest().getParamCount());
        Param requestParam = testData.getCategory(0).getTest(0).getRequest().getParam(0);
        assertEquals("1", requestParam.getName());
        assertEquals("1", requestParam.getValue());
        assertEquals(2, testData.getCategory(0).getTest(0).getResponse().getParam(0).getParamCount());

        // test second query with different value
        ps = c.prepareStatement("SELECT * FROM FOO_TABLE WHERE id = ?");
        ps.setInt(1, 2);
        ps.execute();
        rs = ps.getResultSet();
        testData = captureDataSource.getTestData();
        assertEquals(1, testData.getCategoryCount());
        assertEquals(2, testData.getCategory(0).getTestCount());
        requestParam = testData.getCategory(0).getTest(1).getRequest().getParam(0);
        assertEquals("1", requestParam.getName());
        assertEquals("2", requestParam.getValue());
        assertEquals(2, testData.getCategory(0).getTest(0).getResponse().getParam(0).getParamCount());
    }

    @Test()
    public void testSetNull() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        PreparedStatementResultSetHandler resultSetHandler = connection.getPreparedStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("id");
        resultSet.addColumn("description");
        resultSet.addRow(new Object[] { 1, null });
        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection();
        PreparedStatement ps = c.prepareStatement("SELECT * FROM FOO_TABLE WHERE description = ?");
        ps.setNull(1, Types.VARCHAR);
        ResultSet rs = ps.executeQuery();
        rs.next();
        MockData testData = captureDataSource.getTestData();
        assertEquals("SELECT * FROM FOO_TABLE WHERE description = ?", testData.getCategory(0).getParam(0).getValue());
        assertEquals(1, testData.getCategory(0).getTest(0).getRequest().getParamCount());
        Param requestParam = testData.getCategory(0).getTest(0).getRequest().getParam(0);
        assertEquals("1", requestParam.getName());
        assertEquals("null", requestParam.getValue());
        assertEquals(2, testData.getCategory(0).getTest(0).getResponse().getParam(0).getParamCount());
    }

    @Test()
    public void testExecuteSql() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        StatementResultSetHandler resultSetHandler = connection.getStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("id");
        resultSet.addColumn("description");
        resultSet.addRow(new Object[] { 1, "Description" });
        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection("username", "password");
        Statement statement = c.createStatement();
        statement.execute("SELECT * FROM FOO_TABLE");
        ResultSet rs = statement.getResultSet();
        rs.next();
        MockData testData = captureDataSource.getTestData();
        assertNotNull(testData);
        assertEquals(1, testData.getCategoryCount());
        assertEquals(1, testData.getCategory(0).getParamCount());
        assertEquals("SELECT * FROM FOO_TABLE", testData.getCategory(0).getParam(0).getValue());
        assertEquals(1, testData.getCategory(0).getTestCount());
        assertEquals(0, testData.getCategory(0).getTest(0).getRequest().getParamCount());
        assertEquals(1, testData.getCategory(0).getTest(0).getResponse().getParamCount());
        Param rowParam = testData.getCategory(0).getTest(0).getResponse().getParam(0);
        assertEquals(2, rowParam.getParamCount());
        assertEquals("id", rowParam.getParam(0).getName());
        assertEquals("1", rowParam.getParam(0).getValue());
        assertEquals("description", rowParam.getParam(1).getName());
        assertEquals("Description", rowParam.getParam(1).getValue());
    }

    @Test
    public void verifyStringCase() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        PreparedStatementResultSetHandler resultSetHandler = connection.getPreparedStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("VP1_109_3_");
        resultSet.addColumn("INVOICE2_109_3_");
        resultSet.addColumn("INVOICE3_109_3_");
        List<Object> resultData = new ArrayList<Object>(3);
        for (int i = 0; i < 3; i++) {
            resultData.add("foo");
        }
        resultSet.addRow(resultData.toArray(new Object[0]));

        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection();
        c.setAutoCommit(true);
        String sql = "select invoicesta0_.VP_INVOICE_STAGE_ID as VP1_109_3_, invoicesta0_.INVOICE_NUMBER as INVOICE2_109_3_, "
                + "invoicesta0_.INVOICE_AMOUNT as INVOICE3_109_3_ "
                + "from VP_INVOICE_STAGE invoicesta0_ "
                + "where invoicesta0_.VP_INVOICE_STAGE_ID=?";
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setLong(1, 1);
        ResultSet rs = ps.executeQuery();
        rs.next();
        MockData testData = captureDataSource.getTestData();
        Param rowParam = testData.getCategory(0).getTest(0).getResponse().getParam(0);
        assertEquals("foo", rs.getString("VP1_109_3_"));
        assertEquals("vp_invoice_stage.vp_invoice_stage_id", rowParam.getParam(0).getName());
    }

    @Test
    public void testSetQueryTimeout() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        PreparedStatementResultSetHandler resultSetHandler = connection.getPreparedStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("id");
        resultSet.addColumn("description");
        resultSet.addRow(new Object[] { 1, null });
        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection();
        PreparedStatement ps = c.prepareStatement("SELECT * FROM FOO_TABLE WHERE description = ? and description = ?");

        ps.setNull(2, Types.VARCHAR);
        ps.setQueryTimeout(30);
        ps.setNull(1, Types.VARCHAR);
        assertEquals(ps.getQueryTimeout(), 30);
        ResultSet rs = ps.executeQuery();
        rs.next();
        MockData testData = captureDataSource.getTestData();

        assertEquals("SELECT * FROM FOO_TABLE WHERE description = ? and description = ?", testData.getCategory(0)
                .getParam(0).getValue());
        assertEquals(2, testData.getCategory(0).getTest(0).getRequest().getParamCount());
        Param requestParam = testData.getCategory(0).getTest(0).getRequest().getParam(0);
        assertEquals("2", requestParam.getName());
        assertEquals("null", requestParam.getValue());
        Param requestParam2 = testData.getCategory(0).getTest(0).getRequest().getParam(1);
        assertEquals("1", requestParam2.getName());
        assertEquals("null", requestParam2.getValue());
        assertEquals(2, testData.getCategory(0).getTest(0).getResponse().getParam(0).getParamCount());
    }

    @Test
    public void testSetStringToNull() throws Exception {
        MockDataSource delegateDataSource = new MockDataSource();
        MockConnection connection = new MockConnection();
        PreparedStatementResultSetHandler resultSetHandler = connection.getPreparedStatementResultSetHandler();
        MockResultSet resultSet = resultSetHandler.createResultSet();
        resultSet.addColumn("id");
        resultSet.addColumn("description");
        resultSet.addRow(new Object[] { 1, null });
        resultSetHandler.prepareGlobalResultSet(resultSet);
        delegateDataSource.setupConnection(connection);
        CaptureDataSource captureDataSource = new CaptureDataSource(delegateDataSource);
        Connection c = captureDataSource.getConnection();
        PreparedStatement ps = c.prepareStatement("SELECT * FROM FOO_TABLE WHERE description = ?");

        ps.setString(1, null);

        ResultSet rs = ps.executeQuery();
        rs.next();
        MockData testData = captureDataSource.getTestData();
        Param requestParam2 = testData.getCategory(0).getTest(0).getRequest().getParam(0);
        assertEquals("1", requestParam2.getName());
        assertEquals("null", requestParam2.getValue());

    }
}
