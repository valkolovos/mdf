package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mdf.mockdata.generated.Param;


/**
 * Matches all parameters provided in &lt;request&gt; portion of mock data test against
 * parameters provided by mock data manager. Only parameters provided in &lt;request&gt; need
 * to be matched. Any additional parameters passed in will be ignored.<br/>
 * This is the default matcher for the mock data framework and will be used if no other
 * matcher is specified in configuration.<br/>
 * <br/>
 * Initialization parameter is:<br/>
 * reverse = (boolean) - sets the reverse flag which reverses the match (using the incoming
 * parameters as the test parameters and vice-versa)<br/>
 * It is not required and defaults to "false"<br/>
 * <br/>
 * Example:<br/>
 * Given the following configuration:
 * <pre>
 * &lt;mock-data&gt;
 *   &lt;category&gt;
 *     &lt;test&gt;
 *       &lt;request&gt;
 *         &lt;param name="simple param" value="1"/&gt;
 *         &lt;param name="param with children"&gt;
 *           &lt;param name="child" value="2"/&gt;
 *         &lt;/param&gt;
 *       &lt;/request&gt;
 *     &lt;/test&gt;
 *   &lt;/category&gt;
 * &lt;/mock-data&gt;
 * </pre>
 * The following parameters will match:
 * <pre>
 * &lt;param name="simple param" value="1"/&gt;
 * &lt;param name="param with children"&gt;
 *   &lt;param name="child" value="2"/&gt;
 * &lt;/param&gt;
 * &lt;param name="extra param" value="won't be used for match"&gt;
 * </pre>
 * The following parameters will NOT match:
 * <pre>
 * &lt;param name="simple param" value="1"/&gt;
 * &lt;param name="param with children"&gt;
 *   &lt;param name="child" value="3"/&gt;
 * &lt;/param&gt;
 * </pre>
 */
public class DefaultParameterMatcher implements ParameterMatcher {

    private static final Logger _logger = Logger.getLogger(DefaultParameterMatcher.class);
    private boolean _reverse = false;

    public DefaultParameterMatcher() {
    }

    /**
     * Constructs a DefaultParameterMatcher with the given reverse flag
     * 
     * @param reverse
     *            determines whether to reverse match or not (flip request
     *            and response parameters)
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public DefaultParameterMatcher(boolean reverse) {
        _reverse = reverse;
    }

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if ("reverse".equals(p.getName())) {
                _reverse = Boolean.parseBoolean(p.getValue());
            }
        }
    }

    public boolean paramsMatch(List<Param> requestParams, Param[] testParams) {
        _logger.debug("Checking parameters");
        if (_reverse) {
            return internalParamsMatch(0, Arrays.asList(testParams), requestParams);
        } else {
            return internalParamsMatch(0, requestParams, Arrays.asList(testParams));
        }
    }

    private boolean internalParamsMatch(int indent, List<Param> requestParams, List<Param> testParams) {
        Map<String, List<String>> requestParamMap = new HashMap<String, List<String>>();
        StringBuilder indentPad = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentPad.append("    ");
        }
        for (Param requestParam : requestParams) {
            List<String> valueList = requestParamMap.get(requestParam.getName());
            if (valueList == null) {
                valueList = new ArrayList<String>();
                requestParamMap.put(requestParam.getName(), valueList);
            }
            valueList.add(requestParam.getValue());
        }
        for (Param testParam : testParams) {
            // if there are child parameters, they need to be checked
            if (testParam.getParamCount() > 0) {
                boolean foundChildRequestParam = false;
                for (Param requestParam : requestParams) {
                    if (requestParam.getName().equals(testParam.getName())) {
                        try {
                            if (requestParam.getParamCount() == 0) {
                                continue;
                            }
                            if (internalParamsMatch(indent + 1,
                                    Arrays.asList(requestParam.getParam()), Arrays.asList(testParam.getParam()))) {
                                foundChildRequestParam = true;
                                break;
                            }
                        } catch (NullPointerException npe) {
                            throw new RuntimeException("Caught npe matching parameters " + requestParam.getName()
                                    + ", " + testParam.getName());
                        }
                    }
                }
                if (!foundChildRequestParam) {
                    _logger.debug(indentPad.toString() + "Parameter name that failed to match was "
                            + testParam.getName());
                    return false;
                }
            }

            if (!(requestParamMap.containsKey(testParam.getName()) && requestParamMap.get(testParam.getName())
                    .contains(testParam.getValue()))) {
                _logger.debug(indentPad.toString() + "Parameter name that failed to match was " + testParam.getName()
                        + ". Expected " + testParam.getValue());
                return false;
            }
        }
        return true;
    }

}
