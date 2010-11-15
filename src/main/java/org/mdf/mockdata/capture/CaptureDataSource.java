package org.mdf.mockdata.capture;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.mdf.mockdata.AliasData;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.MockPreparedStatement;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.mdf.mockdata.generated.Test;
import org.springframework.beans.factory.InitializingBean;

public class CaptureDataSource implements DataSource, InitializingBean {

    private DataSource _delegate;
    private MockData _testData;
    private int _catId = 1;
    private String _fileName;
    private String _dumpDir;
    private String _jndiName;

    public CaptureDataSource(DataSource delegate) {
        _delegate = delegate;
        _testData = new MockData();
    }
    
    public void initTestData()
    {
    	_testData = new MockData();
    }

    public Connection getConnection() throws SQLException {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class },
                new ConnectionInvocationHandler(_delegate.getConnection()));
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class },
                new ConnectionInvocationHandler(_delegate.getConnection(username, password)));
    }

    public PrintWriter getLogWriter() throws SQLException {
        return _delegate.getLogWriter();
    }

    public int getLoginTimeout() throws SQLException {
        return _delegate.getLoginTimeout();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        _delegate.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        _delegate.setLoginTimeout(seconds);
    }

    public void writeTestData() throws Exception {
        String fileName = _fileName == null ? "CaptureDataSource.xml" : _fileName;
        String dumpDir = _dumpDir == null ? System.getProperty("orbitz.server.log.dir") : _dumpDir;
        MockDataManager.marshallTestData(_testData, dumpDir + File.separator + fileName);
    }

    public void setFileName(String fileName) {
        _fileName = fileName;
    }

    public void setDumpDir(String dumpDir) {
        _dumpDir = dumpDir;
    }

    public void setJndiName(String jndiName) {
        _jndiName = jndiName;
    }

    public void afterPropertiesSet() throws Exception {
        if (_jndiName != null) {
            InitialContext context = new InitialContext();
            context.bind(_jndiName, this);
        }
    }

    MockData getTestData() {
        return _testData;
    }

    class ConnectionInvocationHandler implements InvocationHandler {

        private Connection _connection;

        public ConnectionInvocationHandler(Connection c) {
            _connection = c;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("prepareStatement")) {
                PreparedStatement ps = (PreparedStatement) method.invoke(_connection, args);
                String sql = (String) args[0];
                Category category = handleCategory(sql);
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { PreparedStatement.class },
                        new PreparedStatementInvocationHandler(ps, category, sql));
            } else if (methodName.equals("createStatement")) {
                Statement statement = (Statement) method.invoke(_connection, args);
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Statement.class },
                        new PreparedStatementInvocationHandler(statement, null, null));
            }
            return method.invoke(_connection, args);
        }

    }

    class PreparedStatementInvocationHandler implements InvocationHandler {

        private Statement _preparedStatement;
        private Category _category;
        private Test _test;
        private String _sql;
        private Map<String, String> _aliasMap;

        public PreparedStatementInvocationHandler(Statement ps, Category category, String sql) {
            _preparedStatement = ps;
            _category = category;
            _sql = sql;
            try {
                AliasData aliasData = MockPreparedStatement.parse(sql != null ? sql.toLowerCase().trim() : sql);
                if (aliasData != null) {
                    _aliasMap = new HashMap<String, String>();
                    for (Map.Entry<String, Set<String>> entry : aliasData.getColumnAliases().entrySet()) {
                        // We want to use tablename.columnname every time
                        String key = entry.getKey();
                        if (key.contains(".")) {
                            Set<String> aliases = entry.getValue();
                            for (String alias : aliases) {
                                _aliasMap.put(alias, key);
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                Logger
                        .getLogger(getClass())
                        .warn(
                                "Unable to parse alias data for sql statement. Will use alias names instead of table names for parameters. Offending SQL: "
                                        + sql, t);
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.startsWith("set") && !methodName.startsWith("setQueryTimeout")) {
                Param requestParam = new Param();
                requestParam.setName(((Integer) args[0]).toString());
                if (methodName.equals("setNull") || args[1]==null) {
                	requestParam.setValue("null");
                } else if(args.length>1){
                	requestParam.setValue(args[1].toString());
                }
                // if this is the first parameter and we haven't already created
                // a test
                if (_test == null) {
                    _test = new Test();
                    Request request = new Request();
                    _test.setRequest(request);
                    Response response = new Response();
                    _test.setResponse(response);
                    _category.addTest(_test);
                }
                _test.getRequest().addParam(requestParam);
            } else if (methodName.startsWith("execute")) {
                String executedSql = _sql;
                if (args != null && args.length == 1) {
                    executedSql = (String) args[0];
                }
                if (_test == null) {
                    _category = handleCategory(executedSql);
                    _test = new Test();
                    Request request = new Request();
                    _test.setRequest(request);
                    Response response = new Response();
                    _test.setResponse(response);
                    _category.addTest(_test);
                }
                if (ResultSet.class.isAssignableFrom(method.getReturnType())) {
                    ResultSet rs = (ResultSet) method.invoke(_preparedStatement, args);
                    return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ResultSet.class },
                            new ResultSetInvocationHandler(rs, _test, _aliasMap));
                }
            } else if (methodName.equals("getResultSet")) {
                ResultSet rs = (ResultSet) method.invoke(_preparedStatement, args);
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ResultSet.class },
                        new ResultSetInvocationHandler(rs, _test, _aliasMap));
            }
            return method.invoke(_preparedStatement, args);
        }

    }

    class ResultSetInvocationHandler implements InvocationHandler {

        private ResultSet _resultSet;
        private Test _test;
        private Map<String, String> _aliasMap;
        private List<String> _columnNames;

        public ResultSetInvocationHandler(ResultSet rs, Test test, Map<String, String> aliasMap) {
            _resultSet = rs;
            _test = test;
            _aliasMap = aliasMap;
            try {
                _columnNames = new ArrayList<String>(rs.getMetaData().getColumnCount());
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    _columnNames.add(rs.getMetaData().getColumnName(i).toLowerCase());
                }
            } catch (Exception e) {
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("next")) {
                boolean b = _resultSet.next();
                if (b) {
                    Set<String> alreadyReturnedColumns = new HashSet<String>();
                    Param rowParam = new Param();
                    rowParam.setName("row");
                    _test.getResponse().addParam(rowParam);
                    for (String columnName : _columnNames) {
                        if (_aliasMap != null && _aliasMap.containsKey(columnName)) {
                            String realName = _aliasMap.get(columnName);
                            // the mock data framework knows how to return multiple
                            // aliases for the same column, so there's no point
                            // in keeping extra copies
                            if (!alreadyReturnedColumns.contains(realName)) {
                                Param responseParam = new Param();
                                responseParam.setName(_aliasMap.get(columnName));
                                responseParam.setValue(_resultSet.getString(columnName));
                                rowParam.addParam(responseParam);
                                alreadyReturnedColumns.add(realName);
                            }
                        } else {
                            Param responseParam = new Param();
                            responseParam.setName(columnName);
                            responseParam.setValue(_resultSet.getString(columnName));
                            rowParam.addParam(responseParam);
                        }
                    }
                }
                return b;
            }
            return method.invoke(_resultSet, args);
        }

    }

    private Category handleCategory(String sql) {
        for (Category category : _testData.getCategory()) {
            Param sqlSnippetParam = category.getParam(0);
            if (sqlSnippetParam.getValue().equals(sql)) {
                return category;
            }
        }
        Category category = new Category();
        category.setName("captured_" + Integer.toString(++_catId));
        Param catParam = new Param();
        catParam.setName("sqlSnippet");
        catParam.setValue(sql);
        category.addParam(catParam);
        _testData.addCategory(category);
        return category;
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        return null;
    }

}
