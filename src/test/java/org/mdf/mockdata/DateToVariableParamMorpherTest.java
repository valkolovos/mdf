package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateMidnight;
import org.mdf.mockdata.generated.Param;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class DateToVariableParamMorpherTest {
    
    private DateToVariableParamMorpher _morpher;
    
    @BeforeTest
    void setup() {
        Param initParam = new Param();
        initParam.setName("morphParamName");
        initParam.setValue("testParam");
        _morpher = new DateToVariableParamMorpher();
        _morpher.setInitParams(initParam);
    }
    
    @Test
    public void morphPositiveDays() throws Exception {
        Param p = new Param();
        p.setName("testParam");
        p.setValue(new DateMidnight().plusDays(1).toString("yyyy-MM-dd'T'HH:mm"));
        assertTrue(_morpher.canMorphParameter(null, p.getName()));
        List<Param> morphedParam = _morpher.morphParameter(p);
        assertEquals(1, morphedParam.size());
        assertNotNull(morphedParam.get(0));
        assertEquals("plusDays", morphedParam.get(0).getParam(0).getName());
        assertEquals("1", morphedParam.get(0).getParam(0).getValue());
    }
    
    @Test
    public void morphNegativeDays() throws Exception {
        Param p = new Param();
        p.setName("testParam");
        p.setValue(new DateMidnight().minusDays(1).toString("yyyy-MM-dd"));
        assertTrue(_morpher.canMorphParameter(null, p.getName()));
        List<Param> morphedParam = _morpher.morphParameter(p);
        assertEquals(1, morphedParam.size());
        assertNotNull(morphedParam.get(0));
        assertEquals("minusDays", morphedParam.get(0).getParam(0).getName());
        assertEquals("1", morphedParam.get(0).getParam(0).getValue());
    }
    
    @Test
    public void noValueParam() throws Exception {
        Param p = new Param();
        p.setName("testParam");
        assertTrue(_morpher.canMorphParameter(null, p.getName()));
        List<Param> morphedParam = _morpher.morphParameter(p);
        assertEquals(1, morphedParam.size());
        assertEquals(p, morphedParam.get(0));
    }
    
    @Test
    public void badValueParam() throws Exception {
        Param p = new Param();
        p.setName("testParam");
        p.setValue("foo");
        assertTrue(_morpher.canMorphParameter(null, p.getName()));
        Exception e = null;
        try {
            _morpher.morphParameter(p);
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
    }
    
    /*
     * Remove when deprecated constructors are removed
     */
    @Test
    public void deprecatedConstructors() throws Exception {
        Param p = new Param();
        p.setName("testParam");
        p.setValue(new DateMidnight().plusDays(1).toString("yyyy-MM-dd"));
        DateToVariableParamMorpher morpher = new DateToVariableParamMorpher("testParam");
        assertTrue(morpher.canMorphParameter(null, p.getName()));
        List<Param> morphedParam = morpher.morphParameter(p);
        assertEquals(1, morphedParam.size());
        assertNotNull(morphedParam.get(0));
        assertEquals("plusDays", morphedParam.get(0).getParam(0).getName());
        assertEquals("1", morphedParam.get(0).getParam(0).getValue());
        
        morpher = new DateToVariableParamMorpher(Arrays.asList(new String[] { "testParam" }));
        assertTrue(morpher.canMorphParameter(null, p.getName()));
        morphedParam = morpher.morphParameter(p);
        assertEquals(1, morphedParam.size());
        assertNotNull(morphedParam.get(0));
        assertEquals("plusDays", morphedParam.get(0).getParam(0).getName());
        assertEquals("1", morphedParam.get(0).getParam(0).getValue());
    }
    
}
