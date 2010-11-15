package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;

import org.mdf.mockdata.generated.Param;
import org.testng.TestRunner;
import org.testng.annotations.Test;


public class StackTraceMatcherTest {
    
    @Test()
    public void testStackMatches() throws Exception {
        StackTraceMatcher matcher = new StackTraceMatcher();
        Param testParam = new Param();
        testParam.setName(StackTraceMatcher.STACK_TRACE_MATCHER);
        Param stackParam = new Param();
        stackParam.setName(getClass().getName());
        stackParam.setValue("testStackMatches");
        testParam.addParam(stackParam);
        assertTrue("Matcher works", matcher.paramsMatch(new ArrayList<Param>(), new Param[] { testParam }));
    }
    
    @Test()
    public void testStackDoesntMatch() throws Exception {
        StackTraceMatcher matcher = new StackTraceMatcher();
        Param testParam = new Param();
        testParam.setName(StackTraceMatcher.STACK_TRACE_MATCHER);
        Param stackParam = new Param();
        stackParam.setName(getClass().getName());
        stackParam.setValue("testStackMatches");
        testParam.addParam(stackParam);
        assertFalse("Matcher works", matcher.paramsMatch(new ArrayList<Param>(), new Param[] { testParam }));
    }
    
    @Test()
    public void testMultipleStackEntries() throws Exception {
        StackTraceMatcher matcher = new StackTraceMatcher();
        Param testParam = new Param();
        testParam.setName(StackTraceMatcher.STACK_TRACE_MATCHER);
        Param stackParam = new Param();
        stackParam.setName(getClass().getName());
        stackParam.setValue("testMultipleStackEntries");
        testParam.addParam(stackParam);
        stackParam = new Param();
        stackParam.setName(TestRunner.class.getName());
        stackParam.setValue("run");
        testParam.addParam(stackParam);
        assertTrue("Matcher works", matcher.paramsMatch(new ArrayList<Param>(), new Param[] { testParam }));
    }
    
    @Test()
    public void testInterface() throws Exception {
        final StackTraceMatcher matcher = new StackTraceMatcher();
        final Param testParam = new Param();
        FooInterface fooImp = new FooInterface() {
            public boolean doMatch() {
                return matcher.paramsMatch(new ArrayList<Param>(), new Param[] { testParam });
            }
        };
        testParam.setName(StackTraceMatcher.STACK_TRACE_MATCHER);
        Param stackParam = new Param();
        stackParam.setName(fooImp.getClass().getName());
        stackParam.setValue("doMatch");
        testParam.addParam(stackParam);
        assertTrue(fooImp.doMatch());
    }
    
    @Test()
    public void testRegEx() throws Exception {
        StackTraceMatcher matcher = new StackTraceMatcher();
        Param testParam = new Param();
        testParam.setName(StackTraceMatcher.STACK_TRACE_MATCHER);
        Param stackParam = new Param();
        stackParam.setName(".*StackTraceMatcherTest");
        stackParam.setValue("testReg..");
        testParam.addParam(stackParam);
        Param useRegExParam = new Param();
        useRegExParam.setName(StackTraceMatcher.USE_REGEX);
        useRegExParam.setValue("true");
        testParam.addParam(useRegExParam);
        assertTrue("Matcher works", matcher.paramsMatch(new ArrayList<Param>(), new Param[] { testParam }));
    }
    
    interface FooInterface {
        boolean doMatch();
    }
}
