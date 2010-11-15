package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.mdf.mockdata.generated.Param;


/**
 *
 * Converts a date parameter to the variable date parameter format used by {@link VariableParamToDateMorpher}. This is useful
 * when request parameters are dates that are variable based on today's date.<br/>
 * Initialization parameters are the names of the parameters to morph. None are required, but at least one is necessary
 * to match.<br/>
 * <br/>
 * Example:
 * <pre>
 * &lt;parameterMorpher class="com.orbitz.servicetests.mockdata.DateToVariableparamMorpher"&gt;
 *   &lt;param name="first param name" value="1"/&gt;
 * &lt;/parameterMorpher&gt;
 * &lt;category&gt;
 *   &lt;test&gt;
 *     &lt;request&gt;
 *       &lt;param name="1"&gt;
 *         &lt;param name="plusDays" value="2"/&gt;
 *       &lt;/param&gt;
 *     &lt;/request&gt;
 *     &lt;/response&gt;
 *   &lt;/test&gt
 * &lt;/category&gt;
 * </pre>
 * This will match request where param 1 has a value of tomorrow. A concrete example might
 * be a SQL select statment that looks like "SELECT * FROM TAB WHERE create_date = sysdate() + 1"
 */
public class DateToVariableParamMorpher implements ParameterMorpher {

    private List<String> _paramNames = new ArrayList<String>();
    
    public DateToVariableParamMorpher() {
    }

    /**
     * @param paramName The name of the parameter to morph
     * @deprecated - Initialize using mock data file and initialization parameters instead
     */
    public DateToVariableParamMorpher(String paramName) {
        _paramNames.add(paramName);
    }

    /**
     * @param paramNames The names of the parameters to morph
     * @deprecated - Initialize using mock data file and initialization parameters instead
     */
    public DateToVariableParamMorpher(List<String> paramNames) {
        _paramNames = paramNames;
    }

    public void setInitParams(Param... initParams) {
        for (Param initParam : initParams) {
            _paramNames.add(initParam.getValue());
        }
    }

    public boolean canMorphParameter(String categoryName, String parameterName) {
        return _paramNames.contains(parameterName);
    }

    public List<Param> morphParameter(Param param) throws Exception {
        List<Param> result = new ArrayList<Param>(1);
        // something is wrong, so just return the original param
        if (param.getValue() == null) {
            result.add(param);
            return result;
        }
        Param returnParam = new Param();
        result.add(returnParam);
        returnParam.setName(param.getName());
        try {
            returnParam.addParam(createDateParam(param.getValue()));
        } catch (IllegalArgumentException iae) {
            throw new Exception("DateToVariableParamMorpher - unable to parse value " + param.getValue(), iae);
        }
        return result;
    }

    private Param createDateParam(String paramValue) {
        DateMidnight d = new DateMidnight(paramValue);
        DateMidnight today = new DateMidnight();
        int dayDiff = new Period(today, d, PeriodType.days()).getDays();

        Param dateParam = new Param();
        if (dayDiff >= 0) {
            dateParam.setName("plusDays");
            dateParam.setValue(Integer.toString(dayDiff));
        } else {
            dateParam.setName("minusDays");
            dateParam.setValue(Integer.toString(Math.abs(dayDiff)));
        }
        return dateParam;
    }

}
