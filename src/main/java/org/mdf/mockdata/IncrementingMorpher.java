package org.mdf.mockdata;

import java.util.Arrays;
import java.util.List;

import org.mdf.mockdata.generated.Param;


/**
 * Increments a value and returns it as the parameter value. This is useful in
 * combination with {@link GenerateMultiplesMorpher} to increment ids.<br/>
 * Initialization parameters are:<br/>
 * initialCount = (integer) - sets count to start incrementing from. default is
 * 1<br/>
 * paramName = (string) - sets name of parameter to morph. default is
 * "Incremental ID"<br/>
 * <br/>
 * <b>since 3.12</b><br/>
 * paramPrefix = (string) - adds a "prefix" value to the resulting parameter
 * value<br/>
 * paramPostfix = (string) - adds a "postfix" value to the resulting parameter
 * value<br/>
 * maxValue = (integer) - maximum value of incrementing. after max is reached, the count will be reset to "initialCount".<br/>
 * useValueAsParamName = (boolean) - if "true", will use the value of the parameter to be morphed
 * as the new parameter name. if "false", will leave the name of the parameter as is. default is "true"><br/>
 * None are required<br/>
 * <br/>
 * Example:
 * 
 * <pre>
 * &lt;parameterMorpher class="com.orbitz.servicetests.mockdata.GenerateMultiplesMorpher"&gt;
 *   &lt;param name="paramPrefix" value="prefix "/&gt;
 *   &lt;param name="paramPostfix" value=" postfix"/&gt;
 *   &lt;param name="useValueAsParamName" value="true"/&gt;
 * &lt;/parameterMorpher&gt;
 * &lt;category&gt;
 *   &lt;test&gt;
 *     &lt;request/&gt;
 *     &lt;response&gt;
 *       &lt;param name="Incremental ID" value="Actual Param Name"/&gt;
 *     &lt;/response&gt;
 *   &lt;/test&gt;
 * &lt;/category&gt;
 * </pre>
 * 
 * returns a response that looks like:
 * 
 * <pre>
 *   &lt;response&gt;
 *     &lt;param name="Actual Param Name" value="prefix 1 postfix"/&gt;
 *   &lt;/response&gt;
 * </pre>
 */
public class IncrementingMorpher implements ParameterMorpher {

    private int _count = 1;
    private int _initialCount = 1;
    private int _maxValue = Integer.MAX_VALUE - 1;
    private String _paramName = "Incremental ID";
    private String _prefix = "";
    private String _postfix = "";
    private boolean _useValueAsParamName = true;

    public IncrementingMorpher() {
    }

    /**
     * Constructs an IncrementingMorpher with an initial count value
     * 
     * @param initialCount
     *            the count value to begin incrementing from
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public IncrementingMorpher(int initialCount) {
        Param countParam = new Param();
        countParam.setName("initialCount");
        countParam.setValue(Integer.toString(initialCount));
        setInitParams(countParam);
    }

    /**
     * Constructs an IncrementingMorpher with the name of the parameter to morph
     * 
     * @param paramName
     *            the name of the parameter to morph
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public IncrementingMorpher(String paramName) {
        Param paramNameParam = new Param();
        paramNameParam.setName("paramName");
        paramNameParam.setValue(paramName);
        setInitParams(paramNameParam);
    }

    /**
     * Constructs an IncrementingMorpher with an initial count value and the
     * name of the parameter to morph
     * 
     * @param initialCount
     *            the count value to begin incrementing from
     * @param paramName
     *            the name of the parameter to morph
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public IncrementingMorpher(int initialCount, String paramName) {
        Param countParam = new Param();
        countParam.setName("initialCount");
        countParam.setValue(Integer.toString(initialCount));
        Param paramNameParam = new Param();
        paramNameParam.setName("paramName");
        paramNameParam.setValue(paramName);
        setInitParams(countParam, paramNameParam);
    }

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if (p.getName().equals("initialCount")) {
                _initialCount = Integer.parseInt(p.getValue());
            } else if (p.getName().equals("paramName")) {
                _paramName = p.getValue();
            } else if (p.getName().equals("paramPrefix")) {
                _prefix = p.getValue();
            } else if (p.getName().equals("paramPostfix")) {
                _postfix = p.getValue();
            } else if (p.getName().equals("maxValue")) {
                _maxValue = Integer.parseInt(p.getValue());
            } else if (p.getName().equals("useValueAsParamName")) {
                _useValueAsParamName = Boolean.parseBoolean(p.getValue());
            }
            _count = _initialCount;
        }
    }

    public boolean canMorphParameter(String categoryName, String parameterName) {
        return _paramName != null && _paramName.equalsIgnoreCase(parameterName);
    }

    public List<Param> morphParameter(Param param) throws Exception {
        Param resultParam = new Param();
        if (_useValueAsParamName) {
            resultParam.setName(param.getValue());
        } else {
            resultParam.setName(param.getName());
        }
        resultParam.setValue(new StringBuilder(_prefix).append(_count++).append(_postfix).toString());
        if (_count > _maxValue) {
            _count = _initialCount;
        }
        return Arrays.asList(new Param[] { resultParam });
    }

    public void setParamName(String paramName) {
        _paramName = paramName;
    }

    public void setCount(int count) {
        _count = count;
    }

}
