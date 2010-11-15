package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.mdf.mockdata.generated.Param;


/**
 * Converts a single parameter into many.<br/>
 * Initialization parameter is:<br/>
 * defaultParameterName = (string) - sets the default parameter name to match<br/>
 * It is not required and defaults to "multiple"<br/>
 * <br/>
 * Example:<br/>
 * <pre>
 *  &lt;parameterMorpher class="com.orbitz.servicetests.mockdata.GenerateMultiplesMorpher"/&gt;
 *  &lt;category&gt;
 *      &lt;test&gt;
 *          &lt;request&gt;
 *              &lt;param name="testName" value="happyPath" /&gt;
 *          &lt;/request&gt;
 *          &lt;response&gt;
 *              &lt;param name="multiple" value="4"&gt;
 *                  &lt;param name="data" value="foo" /&gt;
 *              &lt;/param&gt;
 *          &lt;/response&gt;
 *      &lt;/test&gt;
 *  &lt;/category&gt;
 * </pre>
 * This will create a response that looks like this:
 * <pre>
 *   &lt;response&gt;
 *     &lt;param name="data" value="foo" /&gt;
 *     &lt;param name="data" value="foo" /&gt;
 *     &lt;param name="data" value="foo" /&gt;
 *     &lt;param name="data" value="foo" /&gt;
 *   &lt;/response&gt;
 * </pre>
 * 
 * You can specify the name of the parameter (which defaults to "multiple") this way:
 * <pre>
 *  &lt;parameterMorpher class="com.orbitz.servicetests.mockdata.GenerateMultiplesMorpher"&gt;
 *    &lt;param name="defaultParameterName" value="something other than multiple"/&gt;
 *  &lt;/parameterMorpher&gt;
 * </pre>
 */
public class GenerateMultiplesMorpher implements ChainedParameterMorpher {

    private String _parameterName = "multiple";

    private static final Pattern msgPattern = Pattern.compile("\\{paramName\\}");
    private static final Logger LOG = Logger.getLogger(GenerateMultiplesMorpher.class);

    public GenerateMultiplesMorpher() {
    }

    /**
     * Constructs a GenerateMultiplesMorpher with the given parameter name
     * 
     * @param parameterName
     *            the parameter name to match
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public GenerateMultiplesMorpher(String parameterName) {
        _parameterName = parameterName;
    }

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if ("defaultParameterName".equals(p.getName())) {
                _parameterName = p.getValue();
            }
        }
    }

    public boolean canMorphParameter(String categoryName, String parameterName) {
        return _parameterName.equalsIgnoreCase(parameterName);
    }

    public List<Param> morphParameter(Param param) throws Exception {
        if (param.getParamCount() == 0) {
            logErrorMessage("Param {paramName} has no child params. Returning empty list", param);
            return new ArrayList<Param>(0);
        }
        List<Param> responseList = new ArrayList<Param>();
        int number = 0;
        try {
            number = Integer.parseInt(param.getValue());
        } catch (Exception e) {
            logErrorMessage(
                    "Param {paramName} does not contain a valid 'number' param. An empty list will be returned", param);
        }
        for (int i = 0; i < number; i++) {
            responseList.addAll(Arrays.asList(param.getParam()));
        }
        return responseList;
    }

    public void setParameterName(String parameterName) {
        _parameterName = parameterName;
    }

    private void logErrorMessage(String baseMsg, Param param) {
        LOG.warn(msgPattern.matcher(baseMsg).replaceAll(param.getName()));
    }

}
