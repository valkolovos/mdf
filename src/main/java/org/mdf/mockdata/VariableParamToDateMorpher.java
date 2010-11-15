package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.mdf.mockdata.generated.Param;


/**
 * Provides a way to return variable dates based on today's date. Parameters can be passed in
 * in the mock data to specify date and format. Those parameters are:<br/>
 * * plusDays and value<br/>
 * * minusDays and value<br/>
 * * format - a DateTime string used to format the output. See org.joda.time.format.DateTimeFormat for details<br/>
 * * resultParamName - name for the resulting parameter. Defaults to the name of the passed in parameter.<br/>
 * <br/>
 * plusDays or minusDays is required<br/>
 * format is required<br/>
 * <br/>
 * Initialization parameter is:<br/>
 * defaultParameterName = (string) - sets the default parameter name to match<br/>
 * It is not required and defaults to "Variable Date"<br/>
 * <br/>
 * Example:
 * <pre>
 * &lt;parameterMatcher class="com.orbitz.servicetests.mockdata.VariableParamToDateMorpher"&gt;
 *   &lt;param name="defaultParameterName" value="departureDate"/&gt;
 * &lt;/parameterMorpher&gt;
 * &lt;category&gt;
 *   &lt;test&gt;
 *     &lt;request&gt;
 *       &lt;param name="departureDate"&gt;
 *         &lt;param name="plusDays" value="2"/&gt;
 *         &lt;param name="format" value="yyyy-MM-dd"/&gt;
 *       &lt;/param&gt;
 *     &lt;/request&gt;
 *     &lt;/response&gt;
 *   &lt;/test&gt
 * &lt;/category&gt;
 * </pre>
 * returns a parameter named "departureDate" with a value of today's date plus 2 days
 * in the format provided
 */
public class VariableParamToDateMorpher implements DeferredParameterMorpher {

    private String _paramName = "Variable Date";

    public VariableParamToDateMorpher() {
    }

    /**
     * Constructs a VariableParamToDateMorpher with the given parameter name
     * 
     * @param paramName
     *            the parameter name to match
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public VariableParamToDateMorpher(String paramName) {
        _paramName = paramName;
    }

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if ("defaultParameterName".equals(p.getName())) {
                _paramName = p.getValue();
            }
        }
    }

    public List<Param> morphParameter(Param responseParam) {
        List<Param> result = new ArrayList<Param>();
        if (responseParam.getParamCount() > 0) {
            DateTime date = null;
            String pattern = null;
            Param resultParam = new Param();
            resultParam.setName(responseParam.getName());
            for (Param childParam : responseParam.getParam()) {
                if (childParam.getName().equals("plusDays")) {
                    date = new DateMidnight().plusDays(Integer.parseInt(childParam.getValue())).toDateTime();
                }
                if (childParam.getName().equals("minusDays")) {
                    date = new DateMidnight().minusDays(Integer.parseInt(childParam.getValue())).toDateTime();
                }
                if (childParam.getName().equals("format")) {
                    pattern = childParam.getValue();
                }
                if (childParam.getName().equals("resultParamName")) {
                    resultParam.setName(childParam.getValue());
                }
            }
            if (date != null && pattern != null) {
                resultParam.setValue(date.toString(pattern));
                result.add(resultParam);
            }
        } else {
            result.add(responseParam);
        }
        return result;
    }

    public boolean canMorphParameter(String categoryName, String columnName) {
        return _paramName.equalsIgnoreCase(columnName);
    }

}
