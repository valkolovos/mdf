package org.mdf.mockdata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mdf.mockdata.generated.Param;

import com.mockrunner.jdbc.AbstractOutParameterResultSetHandler;

@SuppressWarnings("unchecked")
public class MockCallableStatement extends com.mockrunner.mock.jdbc.MockCallableStatement {

    private MockDataManager _dataManager;
    private AbstractOutParameterResultSetHandler _resultSetHandler;

    public MockCallableStatement(Connection connection, String sql, MockDataManager dataManager) {
        super(connection, sql);
        _dataManager = dataManager;
    }
    
    protected ResultSet executeQuery(Map params) throws SQLException {
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    protected int executeUpdate(Map params) throws SQLException {
        Map<Object, Object> paramMap = getParameterMap();
        List<Param> requestParams = new ArrayList<Param>();
        for (Map.Entry<Object, Object> entry : paramMap.entrySet()) {
            Param p = new Param();
            p.setName(entry.getKey().toString());
            p.setValue(entry.getValue().toString());
        }
        List<Param> catParams = new ArrayList<Param>();
        Param sqlSnippetParam = new Param();
        catParams.add(sqlSnippetParam);
        sqlSnippetParam.setName("sql");
        sqlSnippetParam.setValue(getSQL().toLowerCase());
        try {
            Param[] sqlResponse = _dataManager.findResponse(catParams, requestParams);
            Map outParameters = new HashMap();
            if (sqlResponse != null) {
                for (Param p : sqlResponse) {
                    try {
                        outParameters.put(Integer.parseInt(p.getName()), p.getValue());
                    } catch (NumberFormatException nfe) {
                        outParameters.put(p.getName(), p.getValue());
                    }
                }
            }
            _resultSetHandler.prepareOutParameter(getSQL(), outParameters);
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
        return 1;
    }

    public void setCallableStatementResultSetHandler(AbstractOutParameterResultSetHandler resultSetHandler) {
        super.setCallableStatementResultSetHandler(resultSetHandler);
        _resultSetHandler = resultSetHandler;
    }

}
