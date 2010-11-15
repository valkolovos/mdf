/**
 * 
 */
package org.mdf.mockdata;

import java.util.List;
import java.util.regex.Pattern;

import org.mdf.mockdata.generated.Param;


/**
 * Uses contains and regular expressions to do parameter matching. This is the default
 * category matcher for the Mock Database implementation of the MDF.<br/>
 * <br/>
 * Initialization parameter is:<br/>
 * defaultParameterName = (string) - sets the default parameter name to match<br/>
 * It is not required and defaults to "sqlSnippet"
 * <br/>
 * Example:<br/>
 * Given the following mock data configuration:
 * 
 * <pre>
 * &lt;mock-data&gt;
 *     &lt;category&gt;
 *         &lt;param name="sqlSnippet" value="FROM TAB_NAME alias"&gt;
 *     &lt;/category&gt;
 *     &lt;test&gt;
 *       ...
 *     &lt;/test&gt;
 * &lt;/mock-data&gt;
 * </pre>
 * 
 * The following SQL statements will match:<br/>
 * SELECT * FROM TAB_NAME alias<br/>
 * SELECT alias.COL_1, alias.COL_2, alias2.COL_1 FROM TAB_NAME alias, OTHER_TAB alias2<br/>
 * <br/>
 * To configure regular expression matching, place a regular expression in the value:
 * <pre>
 * &lt;mock-data&gt;
 *     &lt;category&gt;
 *         &lt;param name="sqlSnippet" value="FROM table *foo .*id ="&gt;
 *     &lt;/category&gt;
 *     &lt;test&gt;
 *       ...
 *     &lt;/test&gt;
 * &lt;/mock-data&gt;
 * </pre>
 * Then the following sql will match:<br/>
 * select * from table   foo where foo.id = 'bar'
 */
public final class SqlSnippetMatcher implements ParameterMatcher {

    private String _parameterName = "sqlSnippet";

    public boolean paramsMatch(List<Param> requestParams, Param[] testParams) {
        String sql = null;
        String sqlSnippet = null;
        for (Param param : requestParams) {
            if (param.getName().equals("sql")) {
                sql = param.getValue();
            }
        }
        for (Param param : testParams) {
            if (_parameterName.equals(param.getName())) {
                sqlSnippet = param.getValue();
            }
        }
        boolean matches = true;
        if (!sql.toUpperCase().contains(sqlSnippet.toUpperCase())) {
            matches = Pattern.compile(sqlSnippet, Pattern.CASE_INSENSITIVE).matcher(sql).find();
        }
        return matches;
    }

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if ("defaultParameterName".equals(p.getName())) {
                _parameterName = p.getValue();
            }
        }
    }
}