package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mdf.mockdata.generated.Param;


/**
 * {@link ParameterMatcher} and {@link ParameterMorpher} that allows for matching on captured data
 * as well as morphing results to captured data. The primary use case for this class is to match
 * on a sequence and return that same value in the results<br/>
 * 
 * To match on an inserted (or updated) row called COLUMN_1 for table TABLE_1, the configuration would look like this:
 * 
 * <pre>
 *   &lt;test&gt;
 *     &lt;request&gt;
 *       &lt;param name="1" value="table TABLE_1 column COLUMN_1"/&gt;
 *     &lt;/request&gt;
 * </pre>
 * 
 * This would match as long as a row has been inserted or updated with the specified column having a value of
 * the request. In the example above, if the request parameter value for the first sql parameter is "foo",
 * the mock data manager will match as long as TABLE_1 has a row that has COLUMN_1 value with "foo".
 * 
 * For morphing, a match must first have been made in the same request. The value of the parameter
 * specifies the morphed parameter name. The configuration looks like this:
 * 
 * <pre>
 *   &lt;test&gt;
 *     &lt;request&gt;
 *       &lt;param name="1" value="table TABLE_1 column COLUMN_1"/&gt;
 *     &lt;/request&gt;
 *     &lt;response&gt;
 *       &lt;param name="table TABLE_1 column COLUMN_1" value="TABLE_1.COLUMN_1"/&gt;
 * </pre>
 * 
 * This will return a parameter named "TABLE_1.COLUMN_1" with the value matched in the request section. In addition, you can
 * morph <b>any</b> column that was inserted with the matching value. For example, if in the above code COLUMN_2 was also inserted,
 * you can return the value of COLUMN_2 that corresponds to COLUMN_1 by doing the following:
 * 
 * <pre>
 *   &lt;test&gt;
 *     &lt;request&gt;
 *       &lt;param name="1" value="table TABLE_1 column COLUMN_1"/&gt;
 *     &lt;/request&gt;
 *     &lt;response&gt;
 *       &lt;param name="table TABLE_1 column COLUMN_2" value="TABLE_1.COLUMN_2"/&gt;
 * </pre>
 * 
 * Initialization parameters are:<br/>
 * delegateParameterMorpher = (string) - the id of the parameter matcher to delegate additional morphing to<br/>
 * It is not required.
 */
public class InsertedDataParameterMatcherMorpher implements DelegatingParameterMatcher, MockDataAwareParameterMatcher, MockDataAwareParameterMorpher, ChainedParameterMorpher {
    
    private ParameterMatcher _delegateParameterMatcher = new DefaultParameterMatcher();
    private MockDataManager _mockDataManager;
    
    private Pattern primaryPattern = Pattern.compile("inserted into table ([^ ]*) column ([^ ]*) value ([^ ]*)");
    private Pattern secondaryPattern = Pattern.compile("table ([^ ]*) column ([^ ]*)");
    
    private static ThreadLocalTableMap t = new ThreadLocalTableMap();
    

    public void setInitParams(Param... initParams) {
    }

    public boolean paramsMatch(List<Param> requestParams, Param[] testParams) {
        List<Param> newTestParams = new ArrayList<Param>(Arrays.asList(testParams));
        if (_mockDataManager != null) {
            for (Iterator<Param> iter = newTestParams.iterator(); iter.hasNext(); ) {
                Param testParam = iter.next();
                if (testParam.getValue() == null) {
                    continue;
                }
                Matcher m = primaryPattern.matcher(testParam.getValue());
                Matcher sm = secondaryPattern.matcher(testParam.getValue());
                if(m.matches())
                {
                	String table = m.group(1);
                    String column = m.group(2);
                    String value = m.group(3);
                    try {
                        Map<String, String> values = DataValidationUtil.getCapturedData(_mockDataManager, table, column, value);
                        if (values.isEmpty()) {
                            return false;
                        } else {
                        	iter.remove();
                        }
                    } catch (Exception e) {
                    }
                }
                else if (sm.matches()) {
                    String value = null;
                    for (Param requestParam : requestParams) {
                        if (requestParam.getName().equals(testParam.getName())) {
                            value = requestParam.getValue();
                            break;
                        }
                    }
                    if (value == null) {
                        return false;
                    }
                    String table = sm.group(1);
                    String column = sm.group(2);
                    try {
                        Map<String, String> values = DataValidationUtil.getCapturedData(_mockDataManager, table, column, value);
                        if (values.isEmpty()) {
                            return false;
                        } else {
                            iter.remove();
                            for (Map.Entry<String, String> entry : values.entrySet()) {
                                t.add(table, entry.getKey(), entry.getValue());
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
            if (_delegateParameterMatcher != null) {
                return _delegateParameterMatcher.paramsMatch(requestParams, newTestParams.toArray(new Param[newTestParams.size()]));
            }
        } else {
            if (_delegateParameterMatcher != null) {
                return _delegateParameterMatcher.paramsMatch(requestParams, testParams);
            }
        }
        return false;
    }
    
    public void setDelegateParameterMatcher(ParameterMatcher delegateParameterMatcher) {
        _delegateParameterMatcher = delegateParameterMatcher;
    }
    
    public void setMockDataManager(MockDataManager mockDataManager) {
        _mockDataManager = mockDataManager;
    }
    
    public boolean canMorphParameter(String categoryName, String parameterName) {
        if (parameterName != null) {
            Matcher m = secondaryPattern.matcher(parameterName);
            return m.matches();
        }
        return false;
    }

    public List<Param> morphParameter(Param param) throws Exception {
        Param newParam = new Param();
        Matcher m = secondaryPattern.matcher(param.getName());
        m.matches();
        String newParamName = m.group(1) + "." + m.group(2);
        String value = t.get().get(m.group(1).toLowerCase()).get(m.group(2).toLowerCase());
        if (param.getValue() != null) {
            newParamName = param.getValue();
        }
        newParam.setName(newParamName);
        newParam.setValue(value);
        List<Param> result = Arrays.asList(new Param[] { newParam });
        return result;
    }
    
    static class ThreadLocalTableMap extends ThreadLocal<Map<String, Map<String, String>>> {
        public Map<String, Map<String, String>> initialValue() {
            return new HashMap<String, Map<String, String>>();
        }
        public void add(String table, String column, String value) {
            Map<String, String> tableMap = get().get(table.toLowerCase());
            if (tableMap == null) {
                tableMap = new HashMap<String, String>();
                get().put(table.toLowerCase(), tableMap);
            }
            tableMap.put(column.toLowerCase(), value);
        }
    }

}
