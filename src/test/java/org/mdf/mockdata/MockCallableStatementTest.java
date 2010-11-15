package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MockCallableStatementTest {

    private MockDataManager _dataManager;
    private MockDataSource _dataSource;

    @BeforeMethod
    public void setUp() throws Exception {
        _dataManager = new MockDataManager("org/mdf/mockdata/MockCallableStatementTestData.xml");
        _dataSource = new MockDataSource(_dataManager);
        _dataSource.afterPropertiesSet();
    }

    @Test()
    public void testBasicStatement() throws Exception {
        Connection c = _dataSource.getConnection();
        CallableStatement cs = c.prepareCall("call insert into dps.package_remark " +
        		"(package_remark_id, remark, user_name, create_date, package_id) " +
        		"values (dps.package_remark_seq.nextval, ?, ?, ?, ?) returning package_remark_id into ? ");
        cs.setString(1, "Remark");
        cs.setString(2, "UserName");
        cs.setTimestamp(3, new Timestamp(new Date().getTime()));
        cs.setLong(4, 42);
        cs.registerOutParameter(5, Types.BIGINT);
        cs.execute();
        assertEquals(1l, cs.getLong(5));
    }

}
