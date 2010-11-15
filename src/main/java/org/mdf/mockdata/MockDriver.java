package org.mdf.mockdata;

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
            _instance._mockDataManager = new MockDataManager();
            _instance._mockDataManager.setCategoryMatcher(new SqlSnippetMatcher());
        } catch (SQLException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private MockDriver() {
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
    
}
