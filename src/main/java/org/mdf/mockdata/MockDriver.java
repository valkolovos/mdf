package org.mdf.mockdata;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("deprecation")
public class MockDriver extends com.mockrunner.mock.jdbc.MockDriver {

    private static MockDriver _instance;
    private MockDataManager _mockDataManager;
    private List<MockConnection> _establishedConnections = Collections.synchronizedList(new ArrayList<MockConnection>());
    private Map<String, Integer> _customFunctionMap = new HashMap<String, Integer>();

    static {
        _instance = new MockDriver();
        try {
            DriverManager.registerDriver(_instance);
            _instance._mockDataManager = new MockDataManager("com/orbitz/servicetests/mockdata/EmptyData.xml");
            _instance._mockDataManager.setCategoryMatcher(new SqlSnippetMatcher());
        } catch (SQLException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private MockDriver() {
        buildCustomFunctionMap();
    }

    public Connection connect(final String url, final Properties info) throws SQLException {
        MockConnection mockConnection = new MockConnection(this);
        mockConnection._mockDataManager = _mockDataManager;
        if (_mockDataManager.getCategoryMatcher() == null) {
            _mockDataManager.setCategoryMatcher(new SqlSnippetMatcher());
        }
        mockConnection.setCustomFunctionMap(_customFunctionMap);
        _establishedConnections.add(mockConnection);
        return mockConnection;
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("jdbc:oracle");
    }
    
    public static synchronized MockDriver getInstance() {
        return _instance;
    }

    public MockDataManager getMockDataManager() {
        return _mockDataManager;
    }

    void resetMockDataManager() {
        for (MockConnection connection : _instance._establishedConnections) {
            connection._mockDataManager = null;
        }
    }
    
    void setMockDataManager(MockDataManager mockDataManager) {
        _instance._mockDataManager = mockDataManager;
        for (MockConnection connection : _instance._establishedConnections) {
            if (connection.getMockDataManager() == null) {
                connection._mockDataManager = mockDataManager;
            }
        }
    }

    void addCustomFunctions(Map<String, Integer> extraFunctions) {
        _customFunctionMap.putAll(extraFunctions);
        for (MockConnection connection : _instance._establishedConnections) {
            if (connection.getMockDataManager() == null) {
                connection.setCustomFunctionMap(_customFunctionMap);
            }
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
