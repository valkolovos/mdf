package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Test;


public class DataValidationUtil {

    private MockDataManager _mockDataManager;

    public DataValidationUtil(MockDataManager mockDataManager) {
        _mockDataManager = mockDataManager;
    }

    /**
     * Utility method to verify data captured
     * 
     * @param table
     *            The table name that should have been updated
     * @param column
     *            The column name (or number) that should have been updated
     * @param value
     *            The value of the column to verify
     * @return true if the mock data manager reports a match
     * @throws Exception
     *             if the mock data manager throws an exception
     */
    public boolean validateCapturedData(String table, String column, String value) throws Exception {
        return validateCapturedData(_mockDataManager, table, column, value);
    }

    /**
     * Utility method to verify data captured. Pass in a map of [column,value]
     * and the mock data manager will check to see if there is an existing
     * captured test that has all the matched parameters.
     * 
     * @param table
     *            The table name that should have been updated
     * @param values
     *            A map of [column,value] to check inserts and updates against
     * @return true if the mock data manager reports a match
     * @throws Exception
     *             if the mock data manager throws an exception
     */
    public boolean validateCapturedData(String table, Map<String, String> values) throws Exception {
        return validateCapturedData(_mockDataManager, table, values);
    }

    /**
     * Utility method to retrieve captured data
     * 
     * @param table
     *            The table name that should have been updated
     * @param column
     *            The column name (or number) that should have been updated
     * @param value
     *            The value of the column to verify
     * @return a map of columns and values for the updated row in the table
     * @throws Exception
     *             if the mock data manager throws an exception
     */
    public Map<String, String> getCapturedData(String table, String column, String value) throws Exception {
        return getCapturedData(_mockDataManager, table, column, value);
    }

    /**
     * Utility method to retrieve captured data
     * 
     * @param dataManager
     *            The data manager to check against
     * @param table
     *            The table name that should have been updated
     * @param values
     *            A map of [column,value] to check inserts and updates against
     * @return a map of columns and values for the updated row in the table
     * @throws Exception
     *             if the mock data manager throws an exception
     */
    public Map<String, String> getCapturedData(String table, Map<String, String> values) throws Exception {
        return getCapturedData(_mockDataManager, table, values);
    }

    /**
     * Utility method to verify data captured
     * 
     * @param dataManager
     *            The data manager to check against
     * @param table
     *            The table name that should have been updated
     * @param column
     *            The column name (or number) that should have been updated
     * @param value
     *            The value of the column to verify
     * @return true if the mock data manager reports a match
     * @throws Exception
     *             if the mock data manager throws an exception
     */
    public static boolean validateCapturedData(MockDataManager dataManager, String table, String column, String value)
            throws Exception {
        Map<String, String> values = new HashMap<String, String>(1);
        values.put(column, value);
        return validateCapturedData(dataManager, table, values);
    }

    /**
     * Utility method to verify data captured. Pass in a map of [column,value]
     * and the mock data manager will check to see if there is an existing
     * captured test that has all the matched parameters.
     * 
     * @param dataManager
     *            The data manager to check against
     * @param table
     *            The table name that should have been updated
     * @param values
     *            A map of [column,value] to check inserts and updates against
     * @return true if the mock data manager reports a match
     * @throws Exception
     *             if the mock data manager throws an exception
     */
    public static boolean validateCapturedData(MockDataManager dataManager, String table, Map<String, String> values)
            throws Exception {
        Param catParam = new Param();
        catParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
        catParam.setValue(table);

        List<Param> columnParams = new ArrayList<Param>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Param columnParam = new Param();
            columnParam.setName(entry.getKey());
            columnParam.setValue(entry.getValue());
            columnParams.add(columnParam);
        }

        return dataManager.verifyCapturedData(Arrays.asList(new Param[] { catParam }), columnParams);
    }

    /**
     * Utility method to retrieve captured data
     * 
     * @param dataManager
     *            The data manager to check against
     * @param table
     *            The table name that should have been updated
     * @param column
     *            The column name (or number) that should have been updated
     * @param value
     *            The value of the column to verify
     * @return a map of columns and values for the updated row in the table
     * @throws Exception
     *             if the mock data manager throws an exception
     */
    public static Map<String, String> getCapturedData(MockDataManager dataManager, String table, String column,
            String value) throws Exception {
        Map<String, String> values = new HashMap<String, String>(1);
        values.put(column, value);
        return getCapturedData(dataManager, table, values);
    }

    /**
     * Utility method to retrieve captured data
     * 
     * @param dataManager
     *            The data manager to check against
     * @param table
     *            The table name that should have been updated
     * @param values
     *            A map of [column,value] to check inserts and updates against
     * @return a map of columns and values for the updated row in the table
     * @throws Exception
     *             if the mock data manager throws an exception
     */
    public static Map<String, String> getCapturedData(MockDataManager dataManager, String table,
            Map<String, String> values) throws Exception {
        Param catParam = new Param();
        catParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
        catParam.setValue(table);

        List<Param> columnParams = new ArrayList<Param>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Param columnParam = new Param();
            columnParam.setName(entry.getKey());
            columnParam.setValue(entry.getValue());
            columnParams.add(columnParam);
        }

        List<Test> results = dataManager.findCapturedData(Arrays.asList(new Param[] { catParam }), columnParams);
        Map<String, String> resultMap = new HashMap<String, String>();
        if (results != null && results.size() > 0) {
            for (Param p : results.get(0).getRequest().getParam()) {
                resultMap.put(p.getName(), p.getValue());
            }
        }
        return resultMap;
    }

}
