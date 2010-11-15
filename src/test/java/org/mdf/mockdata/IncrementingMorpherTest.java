package org.mdf.mockdata;

import java.util.List;

import org.mdf.mockdata.generated.Param;
import org.testng.annotations.Test;


import static org.testng.AssertJUnit.*;

public class IncrementingMorpherTest {
    
    @Test
    public void happyPath() throws Exception {
        IncrementingMorpher morpher = new IncrementingMorpher();
        Param p = new Param();
        p.setName("Incremental ID");
        p.setValue("ActualParamName");
        assertTrue(morpher.canMorphParameter(null, p.getName()));
        List<Param> response = morpher.morphParameter(p);
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("ActualParamName", response.get(0).getName());
        assertEquals("1", response.get(0).getValue());
    }
    
    /*
     * Remove when deprecated constructors are removed 
     */
    @Test
    public void deprecatedConstructorTest() throws Exception {
        Param p = new Param();
        p.setName("Incremental ID");
        p.setValue("ActualParamName");
        IncrementingMorpher morpher = new IncrementingMorpher(2);
        List<Param> response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "2");
        
        p.setName("Different ID");
        morpher = new IncrementingMorpher("Different ID");
        assertTrue(morpher.canMorphParameter(null, p.getName()));
        response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "1");
        
        p.setName("Another ID");
        morpher = new IncrementingMorpher(5, "Another ID");
        assertTrue(morpher.canMorphParameter(null, p.getName()));
        response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "5");
    }
    
    @Test
    public void initializationTest() throws Exception {
        IncrementingMorpher morpher = new IncrementingMorpher();
        
        Param initParam = new Param();
        initParam.setName("initialCount");
        initParam.setValue("2");
        morpher.setInitParams(initParam);
        Param p = new Param();
        p.setName("Incremental ID");
        p.setValue("ActualParamName");
        List<Param> response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "2");
        
        morpher = new IncrementingMorpher();
        initParam = new Param();
        initParam.setName("paramName");
        initParam.setValue("Different ID");
        morpher.setInitParams(initParam);
        p.setName("Different ID");
        assertTrue(morpher.canMorphParameter(null, p.getName()));
        response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "1");
        
        Param countParam = new Param();
        countParam.setName("initialCount");
        countParam.setValue("5");
        Param paramNameParam = new Param();
        paramNameParam.setName("paramName");
        paramNameParam.setValue("Another ID");
        Param prefixParam = new Param();
        prefixParam.setName("paramPrefix");
        prefixParam.setValue("prefix ");
        Param postfixParam = new Param();
        postfixParam.setName("paramPostfix");
        postfixParam.setValue(" postfix");
        Param maxValueParam = new Param();
        maxValueParam.setName("maxValue");
        maxValueParam.setValue("6");
        morpher.setInitParams(countParam, paramNameParam, prefixParam, postfixParam, maxValueParam);
        p.setName("Another ID");
        assertTrue(morpher.canMorphParameter(null, p.getName()));
        response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "prefix 5 postfix");
        response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "prefix 6 postfix");
        response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "prefix 5 postfix");
    }

    @Test
    public void parametersTest() throws Exception {
        Param p = new Param();
        p.setName("Incremental ID");
        p.setValue("ActualParamName");
        IncrementingMorpher morpher = new IncrementingMorpher();
        morpher.setCount(2);
        List<Param> response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "2");
        
        p.setName("Different ID");
        morpher = new IncrementingMorpher();
        morpher.setParamName("Different ID");
        assertTrue(morpher.canMorphParameter(null, p.getName()));
        response = morpher.morphParameter(p);
        verifyParam(response, "ActualParamName", "1");
    }
    
    private void verifyParam(List<Param> response, String expectedName, String expectedValue) throws Exception {
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(expectedName, response.get(0).getName());
        assertEquals(expectedValue, response.get(0).getValue());
    }
}
