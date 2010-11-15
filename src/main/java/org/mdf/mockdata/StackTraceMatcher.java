package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.mdf.mockdata.generated.Param;


/**
 * Matches on the current stack trace. By default, StackTraceMatcher does exact
 * matching. However, to use Regular Expression matching, add a parameter named
 * "useRegEx" to the mock data configuration parameters. This matcher can be
 * used to determine if you are in a specific method before passing additional
 * parameters down to its delegate matcher ({@link DefaultParameterMatcher} by
 * default).<br/>
 * <b>WARNING:</b> Using regular expressions is very inefficient.<br/>
 * <br/>
 * Initialization parameters are:<br/>
 * defaultParameterName = (string) - sets the default parameter name to match.
 * Defaults to StackTraceMatcher<br/>
 * useRegEx = (boolean) - determines whether to use regular expression matching.
 * Default is false<br/>
 * Neither are required<br/>
 * <br/>
 * Example:<br/>
 * To determine if you are in the "doFoo" method of a class instead of the
 * "doFooBar" method, both of which would call the mock data framework with
 * similar parameters but you need to be able to distinguish one from another,
 * you could configure a StackTraceMatcher like this:
 * <pre>
 * &lt;mock-data&gt;
 *     &lt;parameterMatcher class="com.orbitz.servicetests.mockdata.StackTraceMatcher"/&gt;
 *     &lt;category&gt;
 *         &lt;test&gt;
 *             &lt;request&gt;
 *                 &lt;param name="StackTraceMatcher"&gt;
 *                     &lt;param name="com.mockdata.FooService" value="doFoo"/&gt;
 *                 &lt;/param&gt;
 *                 &lt;param name="additionalMatchParam" value="1"/&gt;
 *             &lt;/request&gt;
 *             &lt;response&gt;
 *                 &lt;param name="came from doFoo" value="yes"/&gt;
 *             &lt;/response&gt;
 *         &lt;/test&gt;
 *         &lt;test&gt;
 *             &lt;request&gt;
 *                 &lt;param name="StackTraceMatcher"&gt;
 *                     &lt;param name="com.mockdata.FooService" value="doFooBar"/&gt;
 *                 &lt;/param&gt;
 *                 &lt;param name="additionalMatchParam" value="1"/&gt;
 *             &lt;/request&gt;
 *             &lt;response&gt;
 *                 &lt;param name="came from doFooBar" value="yes"/&gt;
 *             &lt;/response&gt;
 *         &lt;/test&gt;
 *     &lt;/category&gt;
 * &lt;/mock-data&gt;
 * </pre>
 * <b>Note:</b> Prior to version 3.8, the only way to configure the "UseRegEx" flag was
 * to pass an additional parameter in the &lt;test&gt; requeest parameters called
 * "UseRegEx". This is no longer the preferred way to configure this flag and will be
 * removed as an option in the 4.0 line.
 */
public class StackTraceMatcher implements DelegatingParameterMatcher {

    public static final String STACK_TRACE_MATCHER = "StackTraceMatcher";
    public static final String USE_REGEX = "UseRegEx";
    private ParameterMatcher _delegateParamMatcher = new DefaultParameterMatcher();
    private String _parameterName = STACK_TRACE_MATCHER;
    private boolean _useRegEx = false;

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if ("defaultParameterName".equals(p.getName())) {
                _parameterName = p.getValue();
            }
            if ("useRegEx".equals(p.getName())) {
                _useRegEx = Boolean.getBoolean(p.getValue());
            }
        }
    }

    public boolean paramsMatch(List<Param> requestParams, Param[] testParams) {
        List<Param> clonedTestParams = new ArrayList<Param>(Arrays.asList(testParams.clone()));
        for (Iterator<Param> iter = clonedTestParams.iterator(); iter.hasNext();) {
            Param p = iter.next();
            if (p.getName().equals(_parameterName)) {
                boolean useRegex = _useRegEx;
                if (p.getParamCount() == 0) {
                    Logger.getLogger(getClass()).warn(
                            "No stack trace params in StackTraceMatcherParam - returning false");
                    return false;
                }
                for (Param child : p.getParam()) {
                    if (child.getName().equals(USE_REGEX) && Boolean.parseBoolean(child.getValue())) {
                        useRegex = true;
                        break;
                    }
                }
                if (!useRegex) {
                    if (!doStrictMatching(p)) {
                        return false;
                    }
                } else {
                    if (!doRegExMatching(p)) {
                        return false;
                    }
                }
                iter.remove();
            }
        }
        return _delegateParamMatcher.paramsMatch(requestParams, clonedTestParams.toArray(new Param[clonedTestParams
                .size()]));
    }

    public void setDelegateParameterMatcher(ParameterMatcher delegateParamMatcher) {
        _delegateParamMatcher = delegateParamMatcher;
    }

    private boolean doRegExMatching(Param p) {
        Map<Pattern, Pattern> stackMap = new HashMap<Pattern, Pattern>();
        for (Param child : p.getParam()) {
            if (child.getName().equals(USE_REGEX)) {
                continue;
            }
            stackMap.put(Pattern.compile(child.getName()), Pattern.compile(child.getValue()));
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            for (Pattern classPattern : stackMap.keySet()) {
                if (classPattern.matcher(element.getClassName()).matches()) {
                    Pattern methodPattern = stackMap.get(classPattern);
                    if (methodPattern.matcher(element.getMethodName()).matches()) {
                        stackMap.remove(classPattern);
                        break;
                    }
                }
            }
        }
        if (!stackMap.isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean doStrictMatching(Param p) {
        Map<String, String> stackMap = new HashMap<String, String>();
        for (Param child : p.getParam()) {
            if (child.getName().equals(USE_REGEX)) {
                continue;
            }
            stackMap.put(child.getName(), child.getValue());
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String method = stackMap.get(element.getClassName());
            if (method != null && method.equals(element.getMethodName())) {
                stackMap.remove(element.getClassName());
                if (stackMap.isEmpty()) {
                    break;
                }
            }
        }
        if (!stackMap.isEmpty()) {
            return false;
        }
        return true;
    }

}
