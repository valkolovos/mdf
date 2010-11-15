package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.mdf.mockdata.generated.Param;


/**
 * Captures request parameters and uses the captured values to morph reponse parameters.<br/>
 * <br/>
 * Initialization parameters are:<br/>
 * null value = (string) what to set value of parameter to when not found (if not set, param value will be null)<br/>
 * param identifier = (string) the identifier in the mock data parameter name (defaults to "requestParam: ")<br/>
 * param name = (string) multiples of these can be specified to list the parameters that can be morphed. The default
 * behavior is to be able to morph any parameter, returning the original parameter if its value does not contain
 * "param identifier".<br/>
 * param name regex = (string) regular expression to match parameters that can be morphed. if supplied, will override
 * any "param name" initialization parameters that are also supplied.<br/>
 * None are required.<br/>
 * To identify the value to use, the parameter to be morphed must have a value that starts with the value of the "param identifier"
 * initialization parameter (by default, this is set to "requestParam: ").
 * followed by the name of the request parameter. To get a nested request parameter, use dot notation.<br/>
 * Examples:<br/>
 * <br/>
 * <b>Simple Morph</b><br/>
 * Given configuration:
 * <pre>
 *    &lt;parameterMatcher class="com.orbitz.servicetests.mockdata.RequestParameterMatcherMorpher" id="requestParamMM"/&gt;
 *    &lt;parameterMorpher ref="requestParamMM"/&gt;
 *    &lt;category&gt;
        &lt;test&gt;
            &lt;request/&gt;
            &lt;response&gt;
                &lt;param name="param1" value="requestParam: 1"/&gt;
                &lt;param name="param2" value="not morphed"/&gt;
            &lt;/response&gt;
        &lt;/test&gt;
 *    &lt;/category&gt;
 * </pre>
 * and a request with a parameter named "1" having a value of "parameter value"<br/>
 * the parameter "param1" will be returned with a value of "parameter value".<br/>
 * <br/>
 * <b>Nested Parameter Morph</b><br/>
 * Given configuration:
 * <pre>
 *        &lt;test&gt;
 *            &lt;request&gt;
 *                &lt;param name="test nested param" value="true"/&gt;
 *            &lt;/request&gt;
 *            &lt;response&gt;
 *                &lt;param name="param1"&gt;
 *                    &lt;param name="childParam" value="requestParam: requestParam1.childParam"/&gt;
 *                &lt;/param&gt;
 *            &lt;/response&gt;
 *        &lt;/test&gt;
 * </pre>
 * and a request that looks like this:
 * <pre>
 *   &lt;param name="requestParam1"&gt;
 *     &lt;param name="childParam" value="nested param value"/&gt;
 *   &lt;/param&gt;
 * </pre>
 * the parameter "childParam" in the response will have the value "nested param value".
 * 
 * @since 3.12
 *
 */
public class RequestParameterMatcherMorpher implements DelegatingParameterMatcher, DeferredParameterMorpher {
    
    private ParameterMatcher _delegateParameterMatcher;
    private String _paramIdentifier = "requestParam: ";
    private List<String> _paramsToMorph = new ArrayList<String>();
    private Pattern _paramPattern;
    private ThreadLocal<Map<String, String>> t = new ThreadLocal<Map<String, String>>();
    private String nullValue = null;

    public void setDelegateParameterMatcher(ParameterMatcher delegateParameterMatcher) {
        _delegateParameterMatcher = delegateParameterMatcher;
    }

    public boolean paramsMatch(List<Param> requestParams, Param[] testParams) {
        if (_delegateParameterMatcher == null) {
            _delegateParameterMatcher = new DefaultParameterMatcher();
        }
        boolean match = _delegateParameterMatcher.paramsMatch(requestParams, testParams);
        if (match) {
            Map<String, String> paramMap = new HashMap<String, String>();
            for (Param p : requestParams) {
                buildParamMap("", p, paramMap);
            }
            t.set(paramMap);
        }
        return match;
    }

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if (p.getName().equals("null value")) {
                nullValue = p.getValue();
            } else if (p.getName().equals("param identifier")) {
                _paramIdentifier = p.getValue();
            } else if (p.getName().equals("param name")) {
                _paramsToMorph.add(p.getValue());
            } else if (p.getName().equals("param name regex")) {
                _paramPattern = Pattern.compile(p.getValue());
            }
        }
    }

    public boolean canMorphParameter(String categoryName, String parameterName) {
        if (t.get() != null) {
            if (_paramPattern != null) {
                return _paramPattern.matcher(parameterName).find();
            } else if (!_paramsToMorph.isEmpty()) {
                return _paramsToMorph.contains(parameterName);
            }
            return true;
        }
        return false;
    }

    public List<Param> morphParameter(Param param) throws Exception {
        if (param.getValue() == null || !param.getValue().startsWith(_paramIdentifier)) {
            return Arrays.asList(new Param[] { param });
        }
        Param newParam = new Param();
        newParam.setName(param.getName());
        String newValue = t.get().get(param.getValue().substring(_paramIdentifier.length()));
        if (newValue == null) {
            newValue = nullValue;
        }
        newParam.setValue(newValue);
        return Arrays.asList(new Param[] { newParam });
    }
    
    private void buildParamMap(String prefix, Param requestParam, Map<String, String> paramMap) {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(requestParam.getName());
        if (requestParam.getValue() != null) {
            paramMap.put(sb.toString(), requestParam.getValue());
        }
        for (Param child : requestParam.getParam()) {
            buildParamMap(sb.append(".").toString(), child, paramMap);
        }
    }

}
