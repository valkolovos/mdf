package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mdf.mockdata.generated.Param;
import org.testng.annotations.Test;


public class DefaultParameterMatcherTest {
    @Test()
    public void testFoo() throws Exception {
        DefaultParameterMatcher matcher = new DefaultParameterMatcher();
        List<Param> requestParams = new ArrayList<Param>();
        requestParams.add(createParam("Implementation Type", "javax.measure.unit.ProductUnit"));
        requestParams.add(createParam("_elements", null));
        requestParams.add(createParam("_hashCode", "0"));
        Param[] testParams = new Param[] { createParam("Implementation Type", "javax.measure.unit.ProductUnit"),
                createParam("_elements", null), createParam("_hashCode", "0") };
        assertTrue(matcher.paramsMatch(requestParams, testParams));
    }

    @Test()
    public void testReverse() throws Exception {
        DefaultParameterMatcher matcher = new DefaultParameterMatcher();
        Param initParam = createParam("reverse", "true");
        matcher.setInitParams(initParam);
        List<Param> requestParams = new ArrayList<Param>();
        requestParams.add(createParam("Implementation Type", "javax.measure.unit.ProductUnit"));
        requestParams.add(createParam("_elements", null));
        requestParams.add(createParam("_hashCode", "0"));
        Param[] testParams = new Param[] { createParam("Implementation Type", "javax.measure.unit.ProductUnit"),
                createParam("_elements", null) };
        assertFalse(matcher.paramsMatch(requestParams, testParams));
        assertTrue(matcher.paramsMatch(Arrays.asList(testParams), requestParams
                .toArray(new Param[requestParams.size()])));
    }

    @Test
    public void paramValueIsCheckedEvenIfParamsHaveChildren() {
        DefaultParameterMatcher matcher = new DefaultParameterMatcher();

        Param childParam = createParam("attribute", "string");

        Param param1 = createParam("test", "abc");
        param1.addParam(childParam);

        Param param2 = createParam("test", "xyz");
        param2.addParam(childParam);

        assertFalse("Parameters should not have matched", matcher.paramsMatch(Collections.singletonList(param1), new Param[] {param2}));
    }

    @Test
    public void paramValuesAreNullButChildParamsExist() {
        DefaultParameterMatcher matcher = new DefaultParameterMatcher();

        Param childParam = createParam("attribute", "string");

        Param param1 = createParam("test", null);
        param1.addParam(childParam);

        Param param2 = createParam("test", null);
        param2.addParam(childParam);

        assertTrue("Parameters should have matched", matcher.paramsMatch(Collections.singletonList(param1), new Param[] {param2}));
    }

    @Test
    public void paramValuesAreNullNoChildParamsExist() {
        DefaultParameterMatcher matcher = new DefaultParameterMatcher();

        Param param1 = createParam("test", null);

        Param param2 = createParam("test", null);

        assertTrue("Parameters should have matched", matcher.paramsMatch(Collections.singletonList(param1), new Param[] {param2}));
    }

    Param createParam(String name, String value) {
        Param p = new Param();
        p.setName(name);
        p.setValue(value);
        return p;
    }
}
