package org.mdf.mockdata;

import java.util.List;
import java.util.regex.Pattern;

import org.mdf.mockdata.generated.Param;


/**
 * Uses regular expressions to do parameter matching. Delegates, by default, to
 * {@link DefaultParameterMatcher} for parameters not configured for matching.<br/>
 * <br/>
 * Initialization parameter is:<br/>
 * defaultParameterName = (string) - sets the default parameter name to match<br/>
 * It is required<br/>
 * <br/>
 * Example:<br/>
 * Given the following mock data configuration:
 * 
 * <pre>
 * &lt;mock-data&gt;
 *     &lt;parameterMatcher class="com.orbitz.servicetests.mockdata.RegExMatcher"&gt;
 *         &lt;param name="defaultParameterName" value="path"/&gt;
 *     &lt;/parameterMatcher&gt;
 *     &lt;category&gt;
 *         &lt;test&gt;
 *             &lt;request&gt;
 *                 &lt;param name="path" value="\/doSomething/\w*\/necessaryArgument\/\w*\/anotherNecessaryArgument"/&gt;
 *             &lt;/request&gt;
 *             &lt;response&gt;
 *                 &lt;param name="works" value="yes"/&gt;
 *             &lt;/response&gt;
 *         &lt;/test&gt;
 *     &lt;/category&gt;
 * &lt;/mock-data&gt;
 * </pre>
 * 
 * The following parameters will match:
 * 
 * <pre>
 * &lt;param name="path" value="/doSomething/unecessary/necessaryArgument/unecessary/anotherNecessaryArgument"/&gt;
 * </pre>
 * 
 * or
 * 
 * <pre>
 * &lt;param name="path" value="/doSomething/different/necessaryArgument/unecessary/anotherNecessaryArgument"/&gt;
 * </pre>
 * 
 * The following parameters will NOT match:
 * 
 * <pre>
 * &lt;param name="path" value="/doSomething/unecessary/wrongArgument/unecessary/anotherNecessaryArgument"/&gt;
 * </pre>
 * 
 * or
 * 
 * <pre>
 * &lt;param name="path" value="/doSomething/tooShort/necessaryArgument"/&gt;
 * </pre>
 * 
 * @since 3.8
 */
public class RegExMatcher implements DelegatingParameterMatcher {

    private String _parameterName;
    private ParameterMatcher _delegate = new DefaultParameterMatcher();

    public boolean paramsMatch(List<Param> requestParams, Param[] testParams) {
        String requestParamValue = null;
        String testParamValue = null;
        for (Param param : requestParams) {
            if (param.getName().equals(_parameterName)) {
                requestParamValue = param.getValue();
            }
        }
        for (Param param : testParams) {
            if (_parameterName.equals(param.getName())) {
                testParamValue = param.getValue();
            }
        }
        boolean matches = true;
        matches = Pattern.compile(testParamValue, Pattern.CASE_INSENSITIVE).matcher(requestParamValue).find();
        if (!matches) {
            matches = _delegate.paramsMatch(requestParams, testParams);
        }
        return matches;
    }

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if ("defaultParameterName".equals(p.getName())) {
                _parameterName = p.getValue();
            }
        }
        if (_parameterName == null) {
            throw new RuntimeException("init parameter \"defaultParameterName\" not specified for RegExMatcher");
        }
    }

    public void setDelegateParameterMatcher(ParameterMatcher delegateParameterMatcher) {
        _delegate = delegateParameterMatcher;
    }

}
