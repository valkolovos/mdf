package org.mdf.mockdata.remote;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;

import org.mdf.mockdata.MockConnection;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.MockDataSource;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.CategoryMatcher;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RemoteMockPreparedStatementTest {
    
    private NewMockHttpClient _newClient;
    private Connection _conn;
    
    @BeforeMethod
    public void setUp() throws Exception {
        MockDataSource dataSource = new MockDataSource(null);
        dataSource.setRemoteURL("http://not.used:99999");
        _newClient = new NewMockHttpClient(new MockDataManager());
        dataSource.setHttpClient(_newClient);
        _conn = (MockConnection)dataSource.getConnection();
    }

    @Test()
    public void testEmptyResultSet() throws Exception {
        RemoteMockPreparedStatement ps = (RemoteMockPreparedStatement)_conn.prepareStatement("SELECT * FROM FOO");
        ResultSet rs = ps.executeQuery();
        assertFalse(rs.next());
        assertEquals(1, _newClient.requestParameters.size());
        assertEquals("SELECT * FROM FOO", _newClient.requestParameters.get("sql")[0]);
    }

    @Test()
    public void testEmptyResultSetWithRequestParams() throws Exception {
        RemoteMockPreparedStatement ps = (RemoteMockPreparedStatement)_conn.prepareStatement("SELECT * FROM FOO WHERE id=? and descrip=? and active=?");
        ps.setLong(1, 10000l);
        ps.setString(2, "description");
        ps.setBoolean(3, true);

        ResultSet rs = ps.executeQuery();
        assertFalse(rs.next());
        
        assertEquals(4, _newClient.requestParameters.size());
        assertEquals("10000", _newClient.requestParameters.get("param1")[0]);
        assertEquals("description", _newClient.requestParameters.get("param2")[0]);
        assertEquals("true", _newClient.requestParameters.get("param3")[0]);
    }
    
    @Test()
    public void testResultSet() throws Exception {
        RemoteMockPreparedStatement ps = (RemoteMockPreparedStatement)_conn.prepareStatement("SELECT * FROM FOO");
        
        MockData md = new MockData();
        Category cat = new Category();
        md.addCategory(cat);
        
        Param sqlSnippetParam = new Param();
        sqlSnippetParam.setName("sqlSnippet");
        sqlSnippetParam.setValue("select * from foo");
        cat.addParam(sqlSnippetParam);
        org.mdf.mockdata.generated.Test test = new org.mdf.mockdata.generated.Test();
        cat.addTest(test);
        test.setRequest(new Request());
        test.setResponse(new Response());
        
        Param rowParam = new Param();
        rowParam.setName("row");
        test.getResponse().addParam(rowParam);
        Param p = new Param();
        p.setName("id");
        p.setValue("10000");
        rowParam.addParam(p);
        
        p = new Param();
        p.setName("descrip");
        p.setValue("description");
        rowParam.addParam(p);
        
        p = new Param();
        p.setName("active");
        p.setValue("true");
        rowParam.addParam(p);
        
        RemoteMockPreparedStatement oldPs = _newClient.remoteMockData;

        CategoryMatcher matcher = new CategoryMatcher();
        matcher.setClazz("org.mdf.mockdata.SqlSnippetMatcher");
        md.setCategoryMatcher(matcher);
        MockDataManager mockDataManager = new MockDataManager(md);
        _newClient.remoteMockData = new RemoteMockPreparedStatement(mockDataManager);
        
        ResultSet rs = ps.executeQuery();
        assertTrue(rs.next());
        assertEquals(10000l, rs.getLong("id"));
        assertEquals("description", rs.getString(2));
        assertTrue(rs.getBoolean("active"));
        assertFalse(rs.next());
        
        _newClient.remoteMockData = oldPs;
        
    }

    @Test()
    public void testExecuteInsert() throws Exception {
        RemoteMockPreparedStatement ps = (RemoteMockPreparedStatement)_conn.prepareStatement("INSERT INTO FOO VALUES (?, ?, ?)");
        ps.setLong(1, 10000l);
        ps.setString(2, "description");
        ps.setBoolean(3, true);
        int rows = ps.executeUpdate();
        assertEquals(1, rows);
    }
    
    @Test()
    public void testSQLException() throws Exception {
        MockData md = new MockData();
        CategoryMatcher matcher = new CategoryMatcher();
        matcher.setClazz("org.mdf.mockdata.SqlSnippetMatcher");
        md.setCategoryMatcher(matcher);
        Category cat = new Category();
        md.addCategory(cat);
        Param sqlSnippetParam = new Param();
        sqlSnippetParam.setName("sqlSnippet");
        sqlSnippetParam.setValue("select * from foo");
        cat.addParam(sqlSnippetParam);
        org.mdf.mockdata.generated.Test test = new org.mdf.mockdata.generated.Test();
        cat.addTest(test);
        test.setRequest(new Request());
        test.setResponse(new Response());
        
        Param exceptionResponse = new Param();
        exceptionResponse.setName("exception");
        exceptionResponse.setValue("Mock Data Server threw exception - see logs for details");
        test.getResponse().addParam(exceptionResponse);
        
        RemoteMockPreparedStatement oldPs = _newClient.remoteMockData;

        MockDataManager mockDataManager = new MockDataManager(md);
        _newClient.remoteMockData = new RemoteMockPreparedStatement(mockDataManager);

        RemoteMockPreparedStatement ps = (RemoteMockPreparedStatement)_conn.prepareStatement("SELECT * FROM FOO");
        Exception caughtException = null;
        
        try {
            ps.executeQuery();
        } catch (Exception e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
        assertEquals("Mock Data Server threw exception - see logs for details", caughtException.getMessage());
        
        _newClient.remoteMockData = oldPs;

    }
    
    @Test()
    public void testSimpleStatement() throws Exception {
        RemoteMockPreparedStatement ps = (RemoteMockPreparedStatement)_conn.createStatement();


        MockData md = new MockData();
        Category cat = new Category();
        md.addCategory(cat);
        
        Param sqlSnippetParam = new Param();
        sqlSnippetParam.setName("sqlSnippet");
        sqlSnippetParam.setValue("select * from foo");
        cat.addParam(sqlSnippetParam);
        org.mdf.mockdata.generated.Test test = new org.mdf.mockdata.generated.Test();
        cat.addTest(test);
        test.setRequest(new Request());
        test.setResponse(new Response());
        
        Param rowParam = new Param();
        rowParam.setName("row");
        test.getResponse().addParam(rowParam);
        Param p = new Param();
        p.setName("id");
        p.setValue("10000");
        rowParam.addParam(p);
        
        p = new Param();
        p.setName("descrip");
        p.setValue("description");
        rowParam.addParam(p);
        
        p = new Param();
        p.setName("active");
        p.setValue("true");
        rowParam.addParam(p);
        
        RemoteMockPreparedStatement oldPs = _newClient.remoteMockData;

        CategoryMatcher matcher = new CategoryMatcher();
        matcher.setClazz("org.mdf.mockdata.SqlSnippetMatcher");
        md.setCategoryMatcher(matcher);
        MockDataManager mockDataManager = new MockDataManager(md);
        _newClient.remoteMockData = new RemoteMockPreparedStatement(mockDataManager);
        
        ResultSet rs = ps.executeQuery("SELECT * FROM FOO");
        assertTrue(rs.next());
        assertEquals(10000l, rs.getLong("id"));
        assertEquals("description", rs.getString(2));
        assertTrue(rs.getBoolean("active"));
        assertFalse(rs.next());
        
        _newClient.remoteMockData = oldPs;
        
    }
    
    class NewMockHttpClient implements HttpClient {
        
        private RemoteMockPreparedStatement remoteMockData;
        Map<String, String[]> requestParameters;
        
        public NewMockHttpClient(MockDataManager mdm) {
            remoteMockData = new RemoteMockPreparedStatement(mdm);
        }

        public HttpClientResponse doRequest(URL requestURL, Map<String, String[]> requestParameters, byte[] body)
                throws Exception {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.requestParameters = requestParameters;
            remoteMockData.doRequest(requestParameters, null, baos);
            
            HttpClientResponse resp = new HttpClientResponse() {
                
                public int getStatus() {
                    return 200;
                }
                
                public byte[] getResponse() {
                    return baos.toByteArray();
                }
            };
            return resp;
        }
        
    }

}
