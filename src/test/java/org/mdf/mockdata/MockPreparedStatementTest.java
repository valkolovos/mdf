package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.mdf.mockdata.generated.Param;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mockrunner.mock.jdbc.MockArray;

public class MockPreparedStatementTest {

	private MockDataManager _dataManager;
	private MockDataSource _dataSource;

	@BeforeMethod
	public void setUp() throws Exception {
		_dataManager = new MockDataManager(
				"org/mdf/mockdata/MockPreparedStatementTestData.xml");
		_dataSource = new MockDataSource(_dataManager);
		_dataSource.afterPropertiesSet();
	}

	@Test()
	public void testBasicStatement() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("SELECT col1, col2, col3 FROM table_1");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString(1));
		assertEquals("ROW 1 COLUMN 2", rs.getString(2));
		assertNull(rs.getString(3));
		rs.next();
		assertEquals("ROW 2 COLUMN 1", rs.getString("col1"));
		assertEquals("ROW 2 COLUMN 2", rs.getString("col2"));
		assertNull(rs.getString("col3"));
	}

	@Test()
	public void testBasicStatementWithWildcard() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("SELECT * FROM table_1");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString(1));
		assertEquals("ROW 1 COLUMN 2", rs.getString(2));
		assertNull(rs.getString(3));
		rs.next();
		assertEquals("ROW 2 COLUMN 1", rs.getString("col1"));
		assertEquals("ROW 2 COLUMN 2", rs.getString("col2"));
		assertNull(rs.getString("col3"));
	}

	@Test()
	public void testBasicStatementWithParams() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT col1, col2 FROM param_table WHERE param = ?");
		ps.setString(1, "foo");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString(1));
		assertEquals("ROW 1 COLUMN 2", rs.getString(2));
		Exception ex = null;
		try {
			rs.next();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);

		// no rows back with incorrect query
		ps.setString(1, "bar");
		rs = ps.executeQuery();
		try {
			rs.next();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
	}

	@Test()
	public void testBasicStatementWithNullParam() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT col1, col2 FROM param_table WHERE param = ?");
		ps.setNull(1, Types.VARCHAR);
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString(1));
		assertEquals("ROW 1 COLUMN 2", rs.getString(2));
		Exception ex = null;
		try {
			rs.next();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
	}

	@Test()
	public void testBasicStatementWithAliases() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT col1 AS COLUMN_1, col2 AS COLUMN_2 FROM table_1");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString("COLUMN_1"));
		assertEquals("ROW 1 COLUMN 2", rs.getString("COLUMN_2"));
	}

	@Test()
	public void testBasicStatementWithTablePrefix() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("SELECT a.col1, a.col2 FROM table_1 a");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString(1));
		assertEquals("ROW 1 COLUMN 2", rs.getString(2));
		rs.next();
		assertEquals("ROW 2 COLUMN 1", rs.getString("col1"));
		assertEquals("ROW 2 COLUMN 2", rs.getString("col2"));
	}

	@Test()
	public void testHibernateLikeQueryUsingTableAlias() throws Exception {
		Connection c = _dataSource.getConnection();
		int random1 = Math.round((long) (Math.random() * 20));
		int random2 = Math.round((long) (Math.random() * 20));
		while (random2 == random1) {
			random2 = Math.round((long) (Math.random() * 20));
		}
		PreparedStatement ps = c.prepareStatement("SELECT t_1.col1 AS col_1_" + random1
				+ ", t_1.col2 AS col_1_" + random2 + " FROM hib_table t_1");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString("col_1_" + random1));
		assertEquals("ROW 1 COLUMN 2", rs.getString("col_1_" + random2));
	}

	@Test()
	public void testBasicStatementOrder() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("SELECT col1, col2 FROM out_of_order_table");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString(1));
		assertEquals("ROW 1 COLUMN 2", rs.getString(2));
	}

	@Test()
	public void testHibernateLikeQueryUsingRealTableNames() throws Exception {
		Connection c = _dataSource.getConnection();
		int random1 = Math.round((long) (Math.random() * 20));
		int random2 = Math.round((long) (Math.random() * 20));
		while (random2 == random1) {
			random2 = Math.round((long) (Math.random() * 20));
		}
		PreparedStatement ps = c.prepareStatement("SELECT t_1.col1 AS col_1_" + random1
				+ ", t_2.col2 AS col_2_" + random2
				+ " FROM hib_table_1 t_1, hib_table_2 t_2 WHERE param = ?");
		ps.setString(1, "foo");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString("col_1_" + random1));
		assertEquals("ROW 1 COLUMN 2", rs.getString("col_2_" + random2));
	}

	@Test()
	public void testAmbiguousColumnNames() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT a.col1, b.col1 AS COLUMN_2 FROM table_1 a, table_2 b");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString("col1"));
		assertEquals("ROW 1 COLUMN 2", rs.getString("COLUMN_2"));
	}

	@Test()
	public void testUnambiguousColumnNamesWithAliases() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT a.col1 AS COLUMN_1, a.col2 AS COLUMN_2 FROM table_1 a");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString("COLUMN_1"));
		assertEquals("ROW 1 COLUMN 2", rs.getString("COLUMN_2"));
	}

	@Test()
	public void testComplexWildcardStatement() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT a.*, non_wildcard, b.other_non_wildcard FROM table_1 a, table_2 b");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString("column_1"));
		assertEquals("ROW 1 COLUMN 2", rs.getString("column_2"));
		assertEquals("ROW 1 NON WILDCARD", rs.getString("non_wildcard"));
		assertEquals("ROW 1 OTHER NON WILDCARD", rs.getString("other_non_wildcard"));
	}

	@Test()
	public void testBadConfig() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT a.col1, a.col2, b.col2 as b_col2 FROM bad_config a, table_2 b WHERE col1 = ?");
		ps.setString(1, "ambiguous_columns");
		Exception ex = null;
		try {
			ps.executeQuery();
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(ex);
		ps = c
				.prepareStatement("SELECT a_.col1, a_.col2, b_.col2 as b_col2 FROM bad_config a_, table_2 b_ WHERE col1 = ?");
		ps.setString(1, "foo");
		ResultSet rs = ps.executeQuery();
		ex = null;
		String goodColumn = null;
		try {
			rs.next();
			goodColumn = rs.getString("b_col2");
			rs.getString("col1");
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(goodColumn);
		assertNotNull(ex);
	}

	@Test()
	public void testThrowException() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("select * from exception");
		Exception ex = null;
		try {
			ps.executeQuery();
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(ex);
		assertEquals("Got expected class", SQLException.class, ex.getClass());
		assertEquals("Got expected message", "Throwing exception", ex.getMessage());
	}

	@Test()
	public void testSimpleInsert() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("INSERT INTO table1 VALUES (?, ?, ?)");
		ps.setString(1, "value1");
		ps.setString(2, "value2");
		ps.setLong(3, 27);
		ps.executeUpdate();
		Param testParam = new Param();
		testParam.setName("1");
		testParam.setValue("value1");

		Param categoryParam = new Param();
		categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
		categoryParam.setValue("table1");
		assertTrue(_dataManager.verifyCapturedData(Arrays.asList(new Param[] { categoryParam }),
				Arrays.asList(new Param[] { testParam })));
	}

	@Test()
	public void testSpecificColumnInsert() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("INSERT INTO table1 (column1, column2, column3) VALUES (?, ?, ?)");
		ps.setString(1, "value1");
		ps.setString(2, "value2");
		ps.setLong(3, 27);
		ps.executeUpdate();
		Param testParam = new Param();
		testParam.setName("column1");
		testParam.setValue("value1");

		Param categoryParam = new Param();
		categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
		categoryParam.setValue("table1");
		assertTrue(_dataManager.verifyCapturedData(Arrays.asList(new Param[] { categoryParam }),
				Arrays.asList(new Param[] { testParam })));
	}

	@Test()
	public void testHardCodedInsertValue() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("INSERT INTO table1 (column1, column2, column3) VALUES (?, 'value2', ?)");
		ps.setString(1, "value1");
		ps.setLong(2, 27);
		ps.executeUpdate();
		Param testParam = new Param();
		testParam.setName("column1");
		testParam.setValue("value1");
		Param testParam2 = new Param();
		testParam2.setName("column2");
		testParam2.setValue("value2");
		Param testParam3 = new Param();
		testParam3.setName("column3");
		testParam3.setValue("27");

		Param categoryParam = new Param();
		categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
		categoryParam.setValue("table1");
		assertTrue(_dataManager.verifyCapturedData(Arrays.asList(new Param[] { categoryParam }),
				Arrays.asList(new Param[] { testParam, testParam2, testParam3 })));
	}

	@Test()
	public void testOracleCaseStatement() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("SELECT case "
				+ "when table_1.id is not null then 1 " + "when table_2.id is not null then 2 "
				+ "end " + "AS case_statement FROM table_1");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals(1, rs.getInt("case_statement"));

		ps = c.prepareStatement("SELECT case " + "when table_1.id is not null then 1 "
				+ "when table_2.id is not null then 2 " + "end " + "FROM table_1");
		rs = ps.executeQuery();
		rs.next();
		assertEquals(1, rs.getInt("case_statement_1"));
	}

	@Test()
	public void testOracleInClause() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("SELECT * FROM crazy_in_clause_table cict "
				+ "WHERE cict.uk IN ?");
		ps.setString(1, "no_paren");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("value", rs.getString("column"));

		ps = c.prepareStatement("SELECT * FROM crazy_in_clause_table cict "
				+ "WHERE cict.uk_part1 IN ( ?, ?, ? ) AND 1 = 1");
		ps.setString(1, "in1");
		ps.setString(2, "in2");
		ps.setString(3, "in3");
		rs = ps.executeQuery();
		rs.next();
		assertEquals("value", rs.getString("column"));

		ps = c.prepareStatement("SELECT * FROM crazy_in_clause_table cict "
				+ "WHERE (cict.uk_part1, cict.uk_part2) IN ( (?,?), (?,?) ) AND 1 = 1");
		ps.setString(1, "in1_1");
		ps.setString(2, "in1_2");
		ps.setString(3, "in2_1");
		ps.setString(4, "in2_2");
		rs = ps.executeQuery();
		rs.next();
		assertEquals("value", rs.getString("column"));
	}

	@Test()
	public void testSubSelectAndOverClause() throws Exception {
		String sql = "select lead.lead_pricing_id,lead.oneg_hotel_id "
				+ "from (select oneg_hotel_id,lead_pricing_id, "
				+ "      min (low_rate) over (partition by oneg_hotel_id) min_oneg_hotel_id"
				+ "      from oneg_hotel.lead_pricing a where a.oneg_hotel_id in "
				+ "      ( ?  ,?  ,?  ,?  ,?  ) " + "      and a.arrivaldate < sysdate + 30 "
				+ "      and a.modified_date > (sysdate - 2) "
				+ "      and a.ref_point_of_sale_id = ? ) lead "
				+ "where low_rate = min_oneg_hotel_id";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setLong(1, 1001l);
		ps.setLong(2, 1002l);
		ps.setLong(3, 1003l);
		ps.setLong(4, 1004l);
		ps.setLong(5, 1005l);
		ps.setInt(6, 1);
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals(1, rs.getLong("lead_pricing_id"));
		assertEquals(1, rs.getLong("oneg_hotel_id"));
	}

	@Test()
	public void testUnionSelectClause() throws Exception {
		String sql = "select promo.omh_promotion_id, rate.omh_rate_type from omh_prop_rate rate, omh_prop_rate_promo promo "
				+ "where rate.omh_prop_rate_id = promo.omh_prop_rate_id union "
				+ "     select promo.omh_promotion_id, rate.omh_rate_type "
				+ "     from omh_room_rate_promo promo, omh_room_rate roomrate, omh_prop_rate rate "
				+ "     where promo.omh_room_rate_id = roomrate.omh_room_rate_id "
				+ "     and roomrate.omh_prop_rate_id = rate.omh_prop_rate_id";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals(1, rs.getLong("omh_promotion_id"));
		assertEquals("RT", rs.getString("omh_rate_type"));
	}

	@Test()
	public void testMultipleAlises() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT a AS alias_1, a AS alias_2 FROM multi_alias");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("aliased value", rs.getString("alias_1"));
		assertEquals("aliased value", rs.getString("alias_2"));
	}

	@Test()
	public void testOuterJoin() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT a.a_id, a.description a_desc, b.description b_desc, "
						+ "c.description c_desc "
						+ "FROM a left outer join b_table b on a.a_id = b.a_id "
						+ "join c_table c on a.a_id = c.a_id " + "where a.id = ?");
		ps.setString(1, "join");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("join", rs.getString("a_id"));
	}

	@Test()
	public void testComplexBopOuterJoinSQL() throws Exception {
		String sql = "select this_.BOP_DATASET_TYPE_ID as BOP1_31_2_, this_.AUTOPROCESS_FLAG as AUTOPROC2_31_2_, "
				+ "this_.ACTIVE_FLAG as ACTIVE3_31_2_, this_.DELETABLE_FLAG as DELETABLE4_31_2_, this_.AUTO_OVERRIDE_FLAG as AUTO5_31_2_, "
				+ "this_.DESCRIPTION as DESCRIPT6_31_2_, this_.DATASET_TYPE_CODE as DATASET7_31_2_, "
				+ "this_.BOP_EVENT_TYPE_CODE as BOP8_31_2_, this_.BOP_EVENT_SOURCE_CODE as BOP9_31_2_, "
				+ "this_.BOP_BUSINESS_MODEL_CODE as BOP10_31_2_, this_.BOP_POINT_OF_SALE_ID as BOP11_31_2_, "
				+ "this_.REF_PRODUCT_TYPE_CODE as REF12_31_2_, pointofsal2_.BOP_POINT_OF_SALE_ID as BOP1_9_0_, "
				+ "pointofsal2_.POINT_OF_SALE_CODE as POINT2_9_0_, pointofsal2_.POINT_OF_SALE_NAME as POINT3_9_0_, "
				+ "pointofsal2_.REF_POINT_OF_SALE_ID as REF4_9_0_, pointofsal2_.REF_LOCALE_CODE as REF5_9_0_, "
				+ "pointofsal2_.BOP_EVENT_SOURCE_CODE as BOP6_9_0_, pointofsal2_.ACTIVE_FLAG as ACTIVE7_9_0_, "
				+ "pointofsal2_.REF_CURRENCY_CODE as REF8_9_0_, pointofsal2_.POINT_OF_SALE_TIMEZONE as POINT9_9_0_, "
				+ "pointofsal2_.VM_VENDOR_ID as VM10_9_0_, vendor3_.VM_VENDOR_ID as VM1_12_1_, vendor3_.VENDOR_NAME as VENDOR2_12_1_, "
				+ "vendor3_.ADDRESS_LINE1 as ADDRESS3_12_1_, vendor3_.ADDRESS_LINE2 as ADDRESS4_12_1_, vendor3_.CITY_NAME as CITY5_12_1_, "
				+ "vendor3_.STATE_CODE as STATE6_12_1_, vendor3_.COUNTRY_CODE as COUNTRY7_12_1_, vendor3_.ZIP_CODE as ZIP8_12_1_, "
				+ "vendor3_.PHONE_NBR as PHONE9_12_1_, vendor3_.PHONE_EXT as PHONE10_12_1_, vendor3_.FAX_NBR as FAX11_12_1_, "
				+ "vendor3_.CUST_SERV_EMAIL as CUST12_12_1_, vendor3_.CUST_SERV_PHONE_NBR as CUST13_12_1_, "
				+ "vendor3_.CUST_SERV_PHONE_EXT as CUST14_12_1_, vendor3_.CUST_SERV_FAX_NBR as CUST15_12_1_, "
				+ "vendor3_.ACCOUNTING_VENDOR_ID as ACCOUNTING16_12_1_, vendor3_.ACCOUNTING_CREATE_REQUEST_DATE as ACCOUNTING17_12_1_, "
				+ "vendor3_.MODIFIED_DATE as MODIFIED18_12_1_  "
				+ "from BOP_DATASET_TYPE this_  "
				+ "left outer join BOP_POINT_OF_SALE pointofsal2_  on .BOP_POINT_OF_SALE_ID=pointofsal2_.BOP_POINT_OF_SALE_ID  "
				+ "left outer join VM_VENDOR vendor3_  on pointofsal2_.VM_VENDOR_ID=vendor3_.VM_VENDOR_ID  "
				+ "where this_.ACTIVE_FLAG=?  "
				+ "and this_.REF_PRODUCT_TYPE_CODE=?  "
				+ "and this_.BOP_BUSINESS_MODEL_CODE=?  "
				+ "and this_.BOP_EVENT_TYPE_CODE=?  "
				+ "and this_.BOP_EVENT_SOURCE_CODE=?  " + "and this_.BOP_POINT_OF_SALE_ID=?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setInt(1, 1);
		ps.setString(2, "FOO");
		ps.setString(3, "FOO");
		ps.setString(4, "FOO");
		ps.setString(5, "FOO");
		ps.setLong(6, 12345l);
		ps.executeQuery();
	}

	@Test()
	public void testSubSelectAndJoinClause() throws Exception {
		String sql = "select lead.lead_pricing_id,lead.oneg_hotel_id "
				+ "from (select oneg_hotel_id,lead_pricing_id "
				+ "      from oneg_hotel.lead_pricing a "
				+ "      left outer join bogus_table on a.oneg_hotel_id = bogus_table.oneg_hotel_id"
				+ "      where a.oneg_hotel_id in " + "      ( ?  ,?  ,?  ,?  ,?  ) "
				+ "      and a.arrivaldate < sysdate + 30 "
				+ "      and a.modified_date > (sysdate - 2) "
				+ "      and a.ref_point_of_sale_id = ?) lead "
				+ "where low_rate = min_oneg_hotel_id";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setLong(1, 1001l);
		ps.setLong(2, 1002l);
		ps.setLong(3, 1003l);
		ps.setLong(4, 1004l);
		ps.setLong(5, 1005l);
		ps.setInt(6, 1);
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals(1, rs.getLong("lead_pricing_id"));
		assertEquals(1, rs.getLong("oneg_hotel_id"));
	}

	@Test()
	public void testOldStyleOuterJoin() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT a.a_id, a.old_style_outer_join, b.description b_desc FROM a, b WHERE a.a_id = b.a_id (+) AND a.id = ?");
		ps.setString(1, "old style join");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("old style join", rs.getString("a_id"));
	}

	@Test()
	public void testToDateParsing() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT MAX(to_date(foo.date, 'MM/DD/YYYY')) FROM FOO");
		ps.executeQuery();
	}

	@Test()
	public void testUpdate() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("UPDATE table1 SET column1 = ?, column2 = ?, column3 = ? WHERE 1 = 1");
		ps.setString(1, "value1");
		ps.setString(2, "value2");
		ps.setLong(3, 27);
		ps.executeUpdate();
		Param testParam = new Param();
		testParam.setName("column1");
		testParam.setValue("value1");

		Param categoryParam = new Param();
		categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
		categoryParam.setValue("table1");
		assertTrue(_dataManager.verifyCapturedData(Arrays.asList(new Param[] { categoryParam }),
				Arrays.asList(new Param[] { testParam })));
	}

	@Test()
	public void testSqlUpdate() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("update OMH_AVAILABILITY set ROOMS_SOLD =  ROOMS_SOLD - ? where 1 = 1");
		ps.setString(1, "value1");
		ps.executeUpdate();

		Param testParam = new Param();
		testParam.setName("ROOMS_SOLD");
		testParam.setValue("value1");

		Param sqlSnippetParam = new Param();
		sqlSnippetParam.setName("sql snippet");
		sqlSnippetParam
				.setValue("update OMH_AVAILABILITY set ROOMS_SOLD =  ROOMS_SOLD - ? where 1 = 1");

		Param categoryParam = new Param();
		categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
		categoryParam.setValue("OMH_AVAILABILITY");
		assertTrue(_dataManager.verifyCapturedData(Arrays.asList(new Param[] { categoryParam }),
				Arrays.asList(new Param[] { testParam, sqlSnippetParam })));
	}

	@Test()
	public void testHardcodedValueUpdate() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("UPDATE table1 SET column1 = ?, column2 = 27, column3 = ? WHERE 1 = 1");
		ps.setString(1, "value1");
		ps.setString(2, "value2");
		ps.executeUpdate();
		Param testParam = new Param();
		testParam.setName("column1");
		testParam.setValue("value1");
		Param testParam2 = new Param();
		testParam2.setName("column2");
		testParam2.setValue("27");
		Param testParam3 = new Param();
		testParam3.setName("column3");
		testParam3.setValue("value2");

		Param categoryParam = new Param();
		categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
		categoryParam.setValue("table1");
		assertTrue(_dataManager.verifyCapturedData(Arrays.asList(new Param[] { categoryParam }),
				Arrays.asList(new Param[] { testParam, testParam2, testParam3 })));
	}

	@Test()
	public void testDataTypes() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT intgr, vrchr, dt, tmstamp FROM DATA_TYPES");
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals(42, rs.getInt("intgr"));
		assertEquals("What is the question?", rs.getString("vrchr"));
		assertEquals(new Date(new DateTime(2007, 2, 26, 0, 0, 0, 0).getMillis()), rs.getDate("dt"));
		assertEquals(new Timestamp(new DateMidnight().getMillis()), rs.getTimestamp("tmstamp"));
	}

	@Test()
	public void testBopInnerJoinSQL() throws Exception {
		String sql = "select hotelbooke0_.BOP_BOOKING_ID as BOP1_22_, hotelbooke0_1_.ACCOUNTING_DATE as ACCOUNTING2_23_ "
				+ "from BOP_BOOKING hotelbooke0_ "
				+ "inner join BOP_HOTEL_BOOKING_DETAIL hotelbooke0_1_ on hotelbooke0_.BOP_BOOKING_ID=hotelbooke0_1_.BOP_BOOKING_ID "
				+ "where hotelbooke0_.REF_PRODUCT_TYPE_CODE='HOT' "
				+ "and hotelbooke0_.BOP_BUSINESS_MODEL_CODE=? "
				+ "and hotelbooke0_1_.CONFIRMATION_NUMBER=? ";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setString(1, "FOO");
		ps.setInt(2, 1);
		ps.executeQuery();
	}

	@Test()
	public void testNaturalInnerJoinSQL() throws Exception {
		String sql = "SELECT * FROM EMPLOYEES NATURAL INNER JOIN SALARIES ON salary<10000";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeQuery();
	}

	@Test()
	public void testCrossJoinSQL() throws Exception {
		String sql = "SELECT * FROM GameScores CROSS JOIN Departments";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeQuery();
	}

	@Test()
	public void testNaturalFullOuterJoinSQL() throws Exception {
		String sql = "SELECT course_name, period, e.student_name "
				+ "FROM course c NATURAL FULL OUTER JOIN enrollment e;";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeQuery();
	}

	@Test()
	public void testRateAccessSelect() throws Exception {
		String sql = "            select"
				+ "             pr.all_availability_ind"
				+ "           from             omh_property p, omh_prop_rate pr"
				+ "           where p.omh_property_id = pr.omh_property_id"
				+ "           and ( pr.omh_rate_type = 'any' or pr.omh_rate_type = ? )"
				+ "           and pr.omh_prop_rate_id in ("
				+ "             select rr.omh_prop_rate_id"
				+ "             from omh_property p, omh_prop_room_type rt,"
				+ "             omh_room_rate rr"
				+ "             where (p.property_id, p.hotel_chain_code, p.omh_host_code) in"
				+ "                  (             ( ?, ?, ? )         )"
				+ "             and p.omh_property_id = rt.omh_property_id"
				+ "           )"
				+ "           and ( pr.omh_rate_type = 'any' or pr.omh_rate_type = ? )              ";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeQuery();
	}

	@Test()
	public void testDecodeSelect() throws Exception {
		String sql = "select OMH_RESERVATION_ID, OMH.DECODE_CC(SUCC_NUM, SUCC_POOL_ID) as DECODED_SUCC_NUM,"
				+ " SUCC_POOL_ID, SUCC_EXP_DATE, SUCC_USED_DATE,"
				+ "SUCC_CHARGE_LIMIT, SUCC_ACT_DATE, SUCC_DEACT_DATE, SUCC_TRANSMIT_DATE from SUCC where SUCC_ID ="
				+ " (select max(SUCC_ID) from SUCC where OMH_RESERVATION_ID = ?)";
		Map<String, Integer> extraFunctions = new HashMap<String, Integer>();
		extraFunctions.put("omh.decode_cc", 2);
		_dataSource.addCustomFunctions(extraFunctions);
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setLong(1, 1000l);
		ps.executeQuery();
	}

	@Test
	public void inClauseAndInnerJoin() throws Exception {
		String sql = "select this_.FOO as FOO_0_1_ from TEST_METRIC this_ inner join TEST_CAT categories3_ on this_.TEST_METRIC_ID=categories3_.TEST_METRIC_ID inner join TEST_CATEGORY testcatego1_ on categories3_.TEST_CATEGORY_ID=testcatego1_.TEST_CATEGORY_ID where testcatego1_.TEST_CATEGORY_NAME in (?)";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setString(1, "foo");
		ps.executeQuery();
	}

	@Test
	public void autoIncrementedValues() throws Exception {
		List<ParameterMorpher> parameterMorpherList = new ArrayList<ParameterMorpher>(1);
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("test_table_id.nextval", "test_table_id");
		SequenceGeneratingMatcherMorpher morpher = new SequenceGeneratingMatcherMorpher(paramMap);
		parameterMorpherList.add(morpher);
		_dataManager.setParameterMorpherList(parameterMorpherList);
		String sql = "INSERT INTO AUTOGENERATED (ID_COL, NAME_COL) VALUES (?, ?)";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setNull(1, Types.NUMERIC);
		ps.setString(2, "Foo");
		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();
		assertNotNull(rs);
		rs.next();
		assertEquals(1, rs.getLong(1));
	}

	@Test
	public void autoIncrementedValuesOracleStyle() throws Exception {
		List<ParameterMorpher> parameterMorpherList = new ArrayList<ParameterMorpher>(1);
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("id_col_seq.nextval", "test_table_id");
		SequenceGeneratingMatcherMorpher morpher = new SequenceGeneratingMatcherMorpher(paramMap);
		parameterMorpherList.add(morpher);
		_dataManager.setParameterMorpherList(parameterMorpherList);
		String sql = "INSERT INTO ORACLE_AUTOGENERATED (ID_COL, NAME_COL) VALUES (ID_COL_SEQ.NEXTVAL, ?)";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setString(1, "Foo");
		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();
		assertNotNull(rs);
		rs.next();
		assertEquals(1, rs.getLong(1));
	}

	@Test
	public void autoIncrementedValuesWithNoData() throws Exception {
		List<ParameterMorpher> parameterMorpherList = new ArrayList<ParameterMorpher>(1);
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("nodata_table_id.nextval", "nodata_table_id");
		SequenceGeneratingMatcherMorpher morpher = new SequenceGeneratingMatcherMorpher(paramMap);
		parameterMorpherList.add(morpher);
		_dataManager.setParameterMorpherList(parameterMorpherList);
		String sql = "INSERT INTO NODATA_TABLE (ID_COL, NAME_COL) VALUES (?, ?)";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setNull(1, Types.NUMERIC);
		ps.setString(2, "Foo");
		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();
		assertFalse(rs.next());
	}

	@Test
	public void keyConstraintViolationInsert() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("insert into key_constraint ( KEY ) values ( ? )");
		ps.setString(1, "already existing key");
		Exception ex = null;
		try {
			ps.executeUpdate();
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(ex);
		assertEquals("Got expected class", SQLException.class, ex.getClass());
		assertEquals("Got expected message", "Key constraint violation", ex.getMessage());
	}

	@Test
	public void simpleStatement() throws Exception {
		String sql = "SELECT col1, col2, col3 FROM table_1";
		Connection c = _dataSource.getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(sql);
		rs.next();
		assertEquals("ROW 1 COLUMN 1", rs.getString(1));
		assertEquals("ROW 1 COLUMN 2", rs.getString(2));
		assertNull(rs.getString(3));
		rs.next();
		assertEquals("ROW 2 COLUMN 1", rs.getString("col1"));
		assertEquals("ROW 2 COLUMN 2", rs.getString("col2"));
		assertNull(rs.getString("col3"));
	}

	@Test()
	public void updateUsingSimpleStatement() throws Exception {
		Connection c = _dataSource.getConnection();
		Statement statement = c
				.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
		try {
			statement.executeUpdate("SHUTDOWN");
		} catch (Exception e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}

	@Test
	public void testTimeZone() throws Exception {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c
				.prepareStatement("SELECT intgr, vrchr, dt, tmstamp FROM DATA_TYPES");
		ResultSet rs = ps.executeQuery();
		rs.next();

		Calendar c1 = Calendar.getInstance();
		c1.setTime(rs.getTimestamp("tmstamp"));
		Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c2.setTime(rs.getTimestamp("tmstamp", c2));
		Calendar c3 = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
		c3.setTime(rs.getTimestamp(4, c3));
		LocalDateTime ldt1 = LocalDateTime.fromCalendarFields(c1);
		LocalDateTime ldt2 = LocalDateTime.fromCalendarFields(c2);
		LocalDateTime ldt3 = LocalDateTime.fromCalendarFields(c3);
		assertEquals(ldt1, ldt2);
		assertEquals(ldt2, ldt3);
		assertEquals(0, ldt2.getHourOfDay());
	}

	@Test
	public void testAnotherStupidHibernateQuery() throws Exception {
		String sql = "select this_.customer_member_id as customer1_0_3_, this_.anonymous_cookie_value as anonymous2_0_3_, "
				+ "customertr1_.customer_traveler_profile_id as customer1_6_0_, "
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
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setString(1, "foo");
		ps.setString(2, "123-456-7890");
		ps.setLong(3, 27l);
		ResultSet rs = ps.executeQuery();
		rs.next();
		assertEquals("Anon", rs.getString("anonymous2_0_3_"));
	}

	@Test
	public void testSuccNestedSelects() throws Exception {
		String sql = "                      select SUCC_POOL_ID, OMH.DECODE_CC(SUCC_NUM, SUCC_POOL_ID) AS DECODED_SUCC_NUM,             OMH_CC_TYPE_CODE, SUCC_BANK, SUCC_ACCOUNT_NAME,              SUCC_EXP_DATE, MODIFIED_DATE             from SUCC_POOL             where SUCC_USED_FLAG = 0             and SUCC_POOL_ID =                   (select SUCC_POOL_ID from                        (select /*+ index(SUCC_POOL IX02_SUCC_POOL) */ succ_pool_id,                        rank() over (order by succ_pool_id) r                         from SUCC_POOL                        where SUCC_USED_FLAG = 0 and succ_rtb_flag=0                        and SUCC_PREAUTH_FLAG = 0 and to_date(SUCC_EXP_DATE, 'MM/YY') > ?                        )                  where r &lt; 2                 )             for update nowait              ";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

	}

	@Test
	public void testOmhEncodeInsert() throws Exception {
		String sql = "insert into SUCC             (SUCC_ID, OMH_RESERVATION_ID, SUCC_NUM,              SUCC_EXP_DATE, SUCC_USED_DATE, SUCC_CHARGE_LIMIT, SUCC_ACT_DATE,              SUCC_DEACT_DATE, SUCC_POOL_ID, SUCC_CANCEL_DATE, CREATE_DATE, MODIFIED_DATE)          values             (SUCC_SEQ.NEXTVAL, ?, OMH.ENCODE_CC(?, ?),              ?, SYSDATE, ?, ?,              ?, ?, ?, SYSDATE, SYSDATE)";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeUpdate();

	}

	@Test
	public void testAdditionInUpdate() throws Exception {
		String sql = "update OMH_AVAILABILITY set ROOMS_SOLD =  ROOMS_SOLD + ? where OMH_AVAILABILITY_ID = ?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setInt(1, 1);
		ps.executeUpdate();

		List<Param> categoryParams = new ArrayList<Param>();
        List<Param> requestParams=new ArrayList<Param>();
        Param categoryParam = new Param();
        categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
        categoryParam.setValue("OMH_AVAILABILITY");
        categoryParams.add(categoryParam);
        Param requestParam = new Param();
        requestParam.setName("ROOMS_SOLD");
        requestParam.setValue("1");
        requestParams.add(requestParam);
        List<org.mdf.mockdata.generated.Test> tests=_dataManager.findCapturedData(categoryParams, requestParams);
        assertEquals(1,tests.size());
	}

	@Test
	public void testSubtractionInUpdate() throws Exception {
		String sql = "update OMH_AVAILABILITY set ROOMS_SOLD =  ROOMS_SOLD - ? where OMH_AVAILABILITY_ID = ?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeUpdate();
	}

	@Test
	public void testToChar() throws Exception {
		String sql = "SELECT * FROM foo WHERE TO_CHAR(begin_date, 'YYYY/MM/DD') = ?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeUpdate();
	}

	@Test
	public void testTrunc() throws Exception {
		String sql = "SELECT * FROM foo WHERE TRUNC(begin_date) = ?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeQuery();
	}

	@Test
	public void testDecodeAndFnDecrypt() throws Exception {
		String sql = "SELECT ID, EMAIL, EMAILTYPE, PASSWORD, DECODE(PASSWORD_HINT,NULL,NULL,FN_DECRYPT(PASSWORD_HINT, ID)) PASSWORD_HINT,  HOMEAIRCODE, FIRSTNAME, MIDDLENAME, LASTNAME, TITLE, SUFFIX, ADDRESS1, ADDRESS2, CITY, PROVINCE, PROVINCE_CODE, COUNTRY, POSTALCODE, LOGINSTATUS, ACCOUNTSTATUS, IVRPIN, TEMP_PASSWORD, TEMP_PASSWORD_ISSUE_DATE, LASTMODIFIED, MEMBERSHIPSINCE, REGIST_GUEST_IND, GENDER, SHAREDID FROM MEMBER WHERE ID = ?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeQuery();
	}

	@Test
	public void testLowerAndUpper() throws Exception {
		String sql = "SELECT v.ID, v.CORPORATIONID, LOWER(v.EMAIL) EMAIL, v.CORPPERMISSIONS,  v.CREATEDATE, v.LASTMODIFIED, v.MEMBERID, v.FIRSTNAME, v.MIDDLENAME, v.LASTNAME, v.USER_GROUP_ID,  v.INDIVIDUAL_IDENTIFIER  FROM CORPVALIDEMAILNAME_VIEW v WHERE UPPER(v.EMAIL) = ?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeQuery();

	}
	
	@Test
	public void testUnlikelyMultiFunctionUpdate() throws Exception {
	    String sql = "UPDATE ATABLE SET UNLIKELY = SOME_FUNCTION(?) + OTHER_FUNCTION(?) WHERE ID = 'your mom'";
        Connection c = _dataSource.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setLong(1, 100);
        ps.setLong(2, 200);
        ps.executeUpdate();

        List<Param> categoryParams = new ArrayList<Param>();
        List<Param> requestParams=new ArrayList<Param>();
        Param categoryParam = new Param();
        categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
        categoryParam.setValue("ATABLE");
        categoryParams.add(categoryParam);
        List<org.mdf.mockdata.generated.Test> tests=_dataManager.findCapturedData(categoryParams, requestParams);
        assertEquals(1,tests.size());
        assertEquals("SOME_FUNCTION(100)+OTHER_FUNCTION(200)", tests.get(0).getRequest().getParam(0).getValue());
	}

	@Test
	public void testDecodeAndFnEncrypt() throws Exception {
		String sql = "UPDATE MEMBER SET LASTMODIFIED = SYSDATE, EMAIL = ?, EMAILTYPE = ?, PASSWORD = ?, " +
				"PASSWORD_HINT = DECODE(?,NULL,NULL,FN_ENCRYPT(?,?)), HOMEAIRCODE = ?, FIRSTNAME = ?, " +
				"MIDDLENAME = ?, LASTNAME = ?, TITLE = ?, SUFFIX = ?, ADDRESS1 = ?, ADDRESS2 = ?, " +
				"CITY = ?, PROVINCE = ?, PROVINCE_CODE = ?, COUNTRY = ?, POSTALCODE = ?, LOGINSTATUS = ?, " +
				"ACCOUNTSTATUS = ?, IVRPIN = ?, GENDER = ? WHERE ID = ?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setString(1, "email");
		ps.setString(2, "emailType");
		ps.setString(3, "password");
		ps.setString(4, "decode param 1");
		ps.setString(5, "fn_encrypt param 1");
		ps.setString(6, "fn_encrypt param 2");
		ps.setString(7, "homeaircode");
		ps.setString(8, "firstname");
		ps.setNull(9, Types.VARCHAR);
		ps.setString(10, "lastname");
		ps.setNull(11, Types.VARCHAR);
        ps.setNull(12, Types.VARCHAR);
		ps.setString(13, "500 W Madison");
        ps.setNull(14, Types.VARCHAR);
        ps.setString(15, "Chicago");
        ps.setNull(16, Types.VARCHAR);
        ps.setString(17, "IL");
        ps.setString(18, "USA");
        ps.setString(19, "60661");
        ps.setString(20, "loginstatus");
        ps.setString(21, "accountstatus");
        ps.setString(22, "ivrpin");
        ps.setNull(23, Types.VARCHAR);
        ps.setLong(24, 1);
		ps.executeUpdate();

        List<Param> categoryParams = new ArrayList<Param>();
        List<Param> requestParams=new ArrayList<Param>();
        Param categoryParam = new Param();
        categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
        categoryParam.setValue("MEMBER");
        categoryParams.add(categoryParam);
        Param requestParam = new Param();
        requestParam.setName("EMAIL");
        requestParam.setValue("email");
        requestParams.add(requestParam);
        List<org.mdf.mockdata.generated.Test> tests=_dataManager.findCapturedData(categoryParams, requestParams);
        assertEquals(1,tests.size());
        assertEquals("DECODE(decode param 1,NULL,NULL,FN_ENCRYPT(fn_encrypt param 1,fn_encrypt param 2))", tests.get(0).getRequest().getParam(4).getValue());
	}

	@Test
	public void testNotIn() throws Exception {
		
			String sql = "select tp.id as travelplanid, memberid, orbitzlocatorcode, planname, tp.earliestdate as tpearliestdate, latestdate, tp.createdate as tpcreatedate, tp.modifieddate as tpmodifieddate, it.* from travelplan tp, itinerary it where memberid = ? and tp.latestdate >= sysdate and it.travelplanid = tp.id and markfordeletion = 0 and ITINERARYTYPE != 'PRO' and status not in ('cx','fl')";
			Connection c = _dataSource.getConnection();
			PreparedStatement ps = c.prepareStatement(sql);
			ps.executeQuery();
		

	}

	@Test
	public void testSelectAllThing() throws Exception {

			String sql = "select * from (select 0 as user_group_id, corp_image.corp_image_id, corp_image.corp_image_use_code, corp_image.entered_filename,corp_image.physical_filename, corp_image.image_label from corp_image, corp_theme_image_xref x, corp_theme t where t.corp_theme_id =x.corp_theme_id and x.corp_image_id = corp_image.corp_image_id and t.corporation_id = ? and t.corp_default_theme_ind = 1 union all select ug.user_group_id, corp_image.corp_image_id, corp_image.corp_image_use_code, corp_image.entered_filename, corp_image.physical_filename, corp_image.image_label from corp_image, corp_theme_image_xref x, corp_theme t, user_group ug where t.corp_theme_id = x.corp_theme_id and ug.corp_theme_id = t.corp_theme_id and x.corp_image_id = corp_image.corp_image_id and ug.user_group_id = ?) order by 1 asc";
			Connection c = _dataSource.getConnection();
			PreparedStatement ps = c.prepareStatement(sql);
			ps.executeQuery();
	}
	
	@Test
	public void testOsaUpdate() throws Exception {
		String sql = "update itinaddress set pii_address1 = osa.fn_osa_3des_encrypt (?,?), pii_phone1 = osa.fn_osa_3des_encrypt (?,?), pii_phone2 = osa.fn_osa_3des_encrypt (?,?) where id = ?";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.executeUpdate();
	}
	
	@Test
	public void testMerge() throws Exception {
		String sql = "MERGE INTO lead_pricing lpm USING( SELECT ? oneg_hotel_id ,? arrivaldate,? length_of_stay,? ref_point_of_sale_id,? guest_count FROM DUAL)lps ON( lpm.oneg_hotel_id = lps.oneg_hotel_id AND lpm.arrivaldate=lps.arrivaldate AND lpm.length_of_stay=lps.length_of_stay AND lpm.ref_point_of_sale_id=lps.ref_point_of_sale_id AND lpm.guest_count=lps.guest_count)WHEN MATCHED THEN UPDATE SET low_rate = ? ,hostsystem=?,dailyroomrates=? WHEN NOT MATCHED THEN INSERT(oneg_hotel_id,arrivaldate,length_of_stay,low_rate,high_rate,create_date,modified_date,hostsystem,dailyroomrates,ref_point_of_sale_id, guest_count) values(?,?,?,?,?,?,?,?,?,?,?)";
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setString(1,"hi");
		ps.setInt(2,3);
		ps.setNull(4,4);
		ps.setDate(3,new Date(2010,12,12));
		ps.setDouble(5,5d);
		
		ps.executeUpdate();
		
		List<Param> categoryParams = new ArrayList<Param>();
		List<Param> requestParams=new ArrayList<Param>();
		Param categoryParam = new Param();
		categoryParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
		categoryParam.setValue("lead_pricing");
		categoryParams.add(categoryParam);
		Param requestParam = new Param();
		requestParam.setName("2");
		requestParam.setValue("3");
		requestParams.add(requestParam);
		List<org.mdf.mockdata.generated.Test> tests=_dataManager.findCapturedData(categoryParams, requestParams);
		assertEquals(1,tests.size());
	
		
	}
	
	@Test
	public void testSelectUniqueAndHashes() throws Exception {

			String sql = "SELECT unique(p.guar_method) guaranteeMethod,b.omh_prop_billing_method_code billingMethod,p.omh_payment_type_code paymentType,r.corpinfocode corpInfoCode,p.property_message propertyMessage"
        +" FROM"
            +" omh_property p,"
            +" omh_prop_payment b,"
            +" omh_prop_rate r "
            +" WHERE p.omh_property_id = b.omh_property_id"
            +" AND r.omh_property_id = p.omh_property_id"
            +" AND property_id = #hotelId.propertyID#"
            +" AND hotel_chain_code = #hotelId.chainCode#"
            +" AND omh_host_code = #hotelId.host.code#"
            +" AND r.omh_prop_rate_id = #rateCode#"
            +" AND effective_date <= SYSDATE"
            +" AND b.discontinued_date is null"
            +" AND r.is_discontinued = 0";
			Connection c = _dataSource.getConnection();
			try {
			PreparedStatement ps = c.prepareStatement(sql);
			
			
			ps.executeQuery();
			}
			catch (Exception e) {
				
				e.printStackTrace();
				throw e;
			}
	}
	
	@Test
	public void testXmlElement() throws Exception {

			String sql = "select a.oneg_hotel_id onegid, a.chain_code masterchaincode, a.property_name name, a.latitude latitude, a.longitude longitude, a.rating rating,a.amenities amenities, a.postal_code postalcode, a.city city, a.address_ln1 address1, a.address_ln2 address2, a.state_province state,a.country_code country, a.gr_per_prop_score_overall satisfaction,rtrim (xmlagg (xmlelement (e, c.oneg_hotel_id || ',')).extract ('//text()'), ',') losing_oneg_hotel_ids from oneg_hotel.oneg_hotel a ,oneg_hotel .oneg_hotel b ,oneg_hotel .oneg_hotel c where b.oneg_hotel_id = 'foo'  group by a.oneg_hotel_id, " 
				+"a.chain_code,a.property_name, a.latitude, a.longitude, a.rating, a.amenities, a.postal_code,a.city, a.address_ln1, a.address_ln2, a.state_province, "
	 +"a.country_code,a.gr_per_prop_score_overall";
			Connection c = _dataSource.getConnection();
			try {
			PreparedStatement ps = c.prepareStatement(sql);
			
			
			ps.executeQuery();
			}
			catch (Exception e) {
				
				e.printStackTrace();
				throw e;
			}
	}
	
	@Test
	public void testDecodeStatement() throws Exception {
		try {
		Connection c = _dataSource.getConnection();
		PreparedStatement ps = c.prepareStatement("select distinct a.hca_attribute_id attr_id, a.name attr_name,              decode(a.ref_hca_attr_category_code, null, a.name, a.ref_hca_attr_category_code)  attr_category,              cxref.ref_hca_content_type_code attr_type,              decode(cv.ref_hca_content_level_code, 'property',0,'chain',1,'global',2) attr_level,              cs.ref_hca_content_source_code src_name, cs.ref_hca_content_type_code src_type, ca.priority_num             from             hca_attribute a , hca_attr_content_type_xref cxref,             hca_attribute_source cs,             hca_content_aggregation ca,             hca_content_value cv,             hca_content_domain d             where             (cv.oneg_hotel_id=? or cv.oneg_hotel_id is null)             and (cv.chain_code=? or cv.chain_code is null)             and d.ref_language_code=?             and d.ref_country_code=?             and a.hca_attribute_id = cxref.hca_attribute_id             and cv.hca_content_value_id = ca.hca_content_value_id             and ca.hca_attribute_source_id = cs.hca_attribute_source_id             and cs.hca_attribute_id = a.hca_attribute_id             and ca.hca_content_domain_id=d.hca_content_domain_id             and a.active_ind = 1             union             select distinct a.hca_attribute_id attr_id, a.name attr_name, a.ref_hca_attr_category_code attr_category, null, -1              attr_level, null src_name, null src_type, 100000             from             hca_attribute a             where             a.ref_hca_attr_category_code is not null and a.active_ind = 1");
		ps.setInt(1, 1234);
		ps.setString(2, "BW");
		ps.setString(3, "EN");
		ps.setString(4, "US");
		ResultSet rs = ps.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testSpecifiedDataTypes() throws Exception {
	    String sql = "SELECT short_value, integer_value, long_value, double_value, float_value, " +
	    		"big_decimal_value, date_value, time_value, boolean_value, false_value, " +
	    		"timestamp_value, url_value, blob_value, clob_value, array_value " +
	    		"from data_type " +
	    		"where foo = ?";
	    Connection c = _dataSource.getConnection();
	    PreparedStatement ps = c.prepareStatement(sql);
	    ps.setString(1, "data types");
	    ResultSet rs = ps.executeQuery();
	    rs.next();
	    assertEquals(new Short("1"), rs.getObject("short_value"));
        assertEquals(new Short("1"), deserialize(rs.getBytes("short_value")));
        
        assertEquals(new Integer("1"), rs.getObject("integer_value"));
        assertEquals(new Integer("1"), deserialize(rs.getBytes("integer_value")));
        
        assertEquals(new Long("1"), rs.getObject("long_value"));
        assertEquals(new Long("1"), deserialize(rs.getBytes("long_value")));
        
        assertEquals(new Double("1.1"), rs.getObject("double_value"));
        assertEquals(new Double("1.1"), deserialize(rs.getBytes("double_value")));
        
        assertEquals(new Float("1.2"), rs.getObject("float_value"));
        assertEquals(new Float("1.2"), deserialize(rs.getBytes("float_value")));

        assertEquals(new BigDecimal("1.3"), rs.getObject("big_decimal_value"));
        assertEquals(new BigDecimal("1.3"), deserialize(rs.getBytes("big_decimal_value")));

        assertTrue(((Boolean)rs.getObject("boolean_value")).booleanValue());
        assertTrue(((Boolean)deserialize(rs.getBytes("boolean_value"))).booleanValue());

        assertFalse(((Boolean)rs.getObject("false_value")).booleanValue());
        assertFalse(((Boolean)deserialize(rs.getBytes("false_value"))).booleanValue());

        assertEquals(Date.valueOf("1971-03-10"), rs.getObject("date_value"));
        assertEquals(Date.valueOf("1971-03-10"), deserialize(rs.getBytes("date_value")));

        assertEquals(Time.valueOf("23:59:59"), rs.getObject("time_value"));
        assertEquals(Time.valueOf("23:59:59"), deserialize(rs.getBytes("time_value")));

        assertEquals(Timestamp.valueOf("2011-11-11 00:00:00.0001"), rs.getObject("timestamp_value"));
        assertEquals(Timestamp.valueOf("2011-11-11 00:00:00.0001"), deserialize(rs.getBytes("timestamp_value")));

        assertEquals(new URL("http://www.orbitz.com"), rs.getObject("url_value"));
        assertEquals(new URL("http://www.orbitz.com"), deserialize(rs.getBytes("url_value")));

        SerialBlob sBlob = new SerialBlob("This is a blob".getBytes());
        Blob rsBlob = (Blob)rs.getObject("blob_value");
        assertEquals(sBlob.getBytes(1, (int)sBlob.length()), rsBlob.getBytes(1, (int)rsBlob.length()));
        rsBlob = (Blob)deserialize(rs.getBytes("blob_value"));
        assertEquals(sBlob.getBytes(1, (int)sBlob.length()), rsBlob.getBytes(1, (int)rsBlob.length()));

        SerialClob sClob = new SerialClob("This is a clob".toCharArray());
        Clob rsClob = (Clob)rs.getObject("clob_value");
        assertEquals(sClob.getSubString(1, (int)sClob.length()), rsClob.getSubString(1, (int)rsClob.length()));
        rsClob = (Clob)deserialize(rs.getBytes("clob_value"));
        assertEquals(sClob.getSubString(1, (int)sClob.length()), rsClob.getSubString(1, (int)rsClob.length()));
        
        SerialArray serialArray = new SerialArray(new MockArray("ArrayEntry"));
        Array rsArray = (Array)rs.getObject("array_value");
        assertTrue(Arrays.equals(((Object[])serialArray.getArray()), ((Object[])rsArray.getArray())));
        rsArray = (Array)deserialize(rs.getBytes("array_value"));
        assertTrue(Arrays.equals(((Object[])serialArray.getArray()), ((Object[])rsArray.getArray())));
	}
	
	private Object deserialize(byte[] data) throws Exception {
	    ByteArrayInputStream bais = new ByteArrayInputStream(data);
	    ObjectInputStream ois = new ObjectInputStream(bais);
	    return ois.readObject();
	}
}
