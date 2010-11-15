package org.mdf.mockdata.remote;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.MockPreparedStatement;
import org.mdf.mockdata.ParameterMatcher;
import org.mdf.mockdata.SqlSnippetMatcher;

import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockResultSet;

public class RemoteMockPreparedStatement extends MockPreparedStatement implements SimulatorService {

    private HttpClient _httpClient;
    private static final Pattern _sqlParamPattern = Pattern.compile("param([\\d]+)");

    public RemoteMockPreparedStatement(Connection connection, String sql) {
        this(connection, sql, null);
    }
    
    /**
     * This constructor is used on the "client" side of the client / server setup
     * @param connection
     * @param sql
     * @param newClient
     */
    public RemoteMockPreparedStatement(Connection connection, String sql, HttpClient newClient) {
        super(connection, sql, null);
        _sql = sql;
        _httpClient = newClient;
    }
    
    /**
     * This constructor is used on the "server" side
     * @param mockDataManager
     */
    public RemoteMockPreparedStatement(MockDataManager mockDataManager) {
        super(new MockConnection(), null, mockDataManager);
    }
    
    public ResultSet executeQuery(String sql) throws SQLException {
        _sql = sql;
        return executeQuery();
    }
    
    public String getSQL() {
        return _sql;
    }

    @SuppressWarnings("unchecked")
    protected ResultSet executeQuery(Map params) throws SQLException {
        try {
            ObjectInputStream ois = executeRemoteQuery(params);
            Map<String, List<String>> rsMap = (Map<String, List<String>>) ois.readObject();
            MockResultSet rs = buildResultSet(rsMap);
            setResultSets(new ResultSet[] { rs });
            return rs;
        } catch (Exception e) {
            Logger.getLogger(getClass()).error("Remote Query Failed", e);
            throw new SQLException(e.getMessage());
        }
    }

    private MockResultSet buildResultSet(Map<String, List<String>> rsMap) {
        MockResultSet rs = new MockResultSet("mock");
        for (Map.Entry<String, List<String>> entry : rsMap.entrySet()) {
            rs.addColumn(entry.getKey(), entry.getValue());
        }
        return rs;
    }

    @SuppressWarnings("unchecked")
    protected int executeUpdate(Map params) throws SQLException {
        try {
            ObjectInputStream ois = executeRemoteQuery(params);
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                setLastGeneratedKeysResultSet(buildResultSet((Map<String, List<String>>)obj));
                return 1;
            } else {
                int result = (Integer) obj;
                return result;
            }
        } catch (Exception e) {
            Logger.getLogger(getClass()).error("Remote Query Failed", e);
            throw new SQLException(e.getMessage());
        }
    }
    
    void setNewHttpClient(HttpClient client) {
        _httpClient = client;
    }

    @SuppressWarnings("unchecked")
    private ObjectInputStream executeRemoteQuery(Map params) throws Exception {
        
        Logger logger = Logger.getLogger(getClass());
        Map<String, String[]> requestParameters = new HashMap<String, String[]>();
        requestParameters.put("sql", new String[] { getSQL() });
        if (params != null) {
            for (Iterator<Map.Entry<Object, Object>> iter = params.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<Object, Object> entry = iter.next();
                requestParameters.put("param" + (entry.getKey().toString()), new String[] { entry.getValue() == null ? "null" : entry.getValue().toString() });
            }
        }
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Executing remote SQL query: ");
            sb.append(getSQL());
            sb.append("\n");
            if (params != null) {
                for (Iterator<Map.Entry<Object, Object>> iter = params.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry<Object, Object> entry = iter.next();
                    sb.append("    ").append(entry.getKey().toString()).append(": ");
                    if (entry.getValue() == null) {
                        sb.append("null");
                    } else {
                        sb.append(entry.getValue().toString());
                    }
                    sb.append("\n");
                }
            }
            logger.debug(sb.toString());
        }
        HttpClientResponse resp = _httpClient.doRequest(new URL(getConnection().getMetaData().getURL()),
                requestParameters, null);
        if (resp.getStatus() != 200) {
            StringBuilder sb = new StringBuilder();
            if (resp.getResponse() == null) {
                sb.append("Mock Data Server returned invalid response. Possible timeout. Status was: ");
                sb.append(resp.getStatus());
            } else {
                sb.append("Mock Data Server threw exception: ");
                sb.append(new String(resp.getResponse()));
            }
            logger.error(sb.toString());
            throw new SQLException("Mock Data Server threw exception - see logs for details");
        }
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(resp.getResponse()));
        return ois;
    }
    
    public void doRequest(Map<String, String[]> requestParams, InputStream is, OutputStream os) throws Exception {
        _sql = requestParams.get("sql")[0];
        Map<String, String> sqlParams = new HashMap<String, String>();
        for (String s : requestParams.keySet()) {
            Matcher m = _sqlParamPattern.matcher(s);
            if (m.matches()) {
                sqlParams.put(m.group(1), requestParams.get(s)[0]);
            }
        }
        ObjectOutputStream objectOut = new ObjectOutputStream(os);
        if (_sql.toLowerCase().startsWith("update") || _sql.toLowerCase().startsWith("insert")) {
            int response = super.executeUpdate(sqlParams);
            if (getGeneratedKeys().next()) {
                writeResultSet(getGeneratedKeys(), objectOut);
            } else {
                objectOut.writeObject(new Integer(response));
            }
        } else {
            ResultSet rs = super.executeQuery(sqlParams);
            writeResultSet(rs, objectOut);
        }
    }

    @SuppressWarnings("deprecation")
    public void setMockData(String fileName) throws Exception {
        ParameterMatcher existingCategoryMatcher = _dataManager.getCategoryMatcher();
        _dataManager.loadTestDataFromFile(fileName);
        if (_dataManager.getCategoryMatcher() == null) {
            if (existingCategoryMatcher == null) {
                _dataManager.setCategoryMatcher(new SqlSnippetMatcher());
            } else {
                _dataManager.setCategoryMatcher(existingCategoryMatcher);
            }
        }
    }

    private void writeResultSet(ResultSet rs, ObjectOutputStream objectOut) throws Exception {
        ResultSetMetaData metadata = rs.getMetaData();
        Map<String, List<String>> rsMap = new HashMap<String, List<String>>();
        while (rs.next()) {
            for (int i = 0; i < metadata.getColumnCount(); i++) {
                List<String> data = rsMap.get(metadata.getColumnName(i + 1));
                if (data == null) {
                    data = new ArrayList<String>();
                    rsMap.put(metadata.getColumnName(i + 1), data);
                }
                data.add(rs.getString(i + 1));
            }
        }
        objectOut.writeObject(rsMap);
    }

}
