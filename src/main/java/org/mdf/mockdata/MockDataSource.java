package org.mdf.mockdata;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.mdf.mockdata.remote.HttpClient;
import org.springframework.beans.factory.InitializingBean;



public class MockDataSource extends com.mockrunner.mock.jdbc.MockDataSource
        implements InitializingBean {
    private MockDataManager _mockDataManager;
    private String _jndiName;
    private String _remoteURL;
    private HttpClient _httpClient;
    private Map<String, Integer> _customFunctionMap = new HashMap<String, Integer>();
    
    public MockDataSource(MockDataManager mockDataManager) {
        _mockDataManager = mockDataManager;
        buildCustomFunctionMap();
    }

    public Connection getConnection() throws SQLException {
        return getConnection(null, null);
    }

    @SuppressWarnings("deprecation")
    public Connection getConnection(String username, String password)
        throws SQLException {
        MockConnection connection = new MockConnection(_mockDataManager, _remoteURL, _httpClient);
        if (_mockDataManager != null && _mockDataManager.getCategoryMatcher() == null) {
            _mockDataManager.setCategoryMatcher(new SqlSnippetMatcher());
        }
        connection.setCustomFunctionMap(_customFunctionMap);
        return connection;
    }
    
    public void setJndiName(String jndiName) {
        _jndiName = jndiName;
    }
    
    public void setRemoteURL(String remoteURL) {
        _remoteURL = remoteURL;
    }
    
    public void setHttpClient(HttpClient httpClient) {
        _httpClient = httpClient;
    }
    
    public void addCustomFunctions(Map<String, Integer> extraFunctions) {
        _customFunctionMap.putAll(extraFunctions);
    }
    
    @SuppressWarnings("deprecation")
    public void afterPropertiesSet() throws Exception {
        if (_mockDataManager.getCategoryMatcher() == null) {
            _mockDataManager.setCategoryMatcher(new SqlSnippetMatcher());
        }
        if (_jndiName != null) {
            Context ctx = new InitialContext();
            ctx.bind(_jndiName, this);
        }
    }

    private void buildCustomFunctionMap() {
        String customFunctionFile = "/mockPreparedStatement.customFunctions";
        if (System.getProperty("customFunctionFile") != null) {
            customFunctionFile = System.getProperty("customFunctionFile");
        }
        InputStream is = MockPreparedStatement.class.getResourceAsStream(customFunctionFile);
        byte[] buffer = new byte[4096];
        int c;
        StringBuilder sb = new StringBuilder();
        try {
            while ((c = is.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, c));
            }
            is.close();
            String[] lines = sb.toString().split("\n");
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] keyValue = line.split(",");
                _customFunctionMap.put(keyValue[0], Integer.parseInt(keyValue[1]));
            }
        } catch (IOException e) {
        }
    }

}
