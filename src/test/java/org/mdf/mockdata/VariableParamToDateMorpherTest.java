package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.joda.time.DateMidnight;
import org.mdf.mockdata.generated.Param;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class VariableParamToDateMorpherTest {
    
    VariableParamToDateMorpher _morpher;
    
    @BeforeTest
    void setUp() throws Exception {
        _morpher = new VariableParamToDateMorpher();
        Param defaultParamName = new Param();
        defaultParamName.setName("defaultParameterName");
        defaultParamName.setValue("ParamName");
        _morpher.setInitParams(defaultParamName);
    }

    @Test
    public void plusDays() throws Exception {
        Param configParam = new Param();
        configParam.setName("ParamName");
        Param plusDaysParam = new Param();
        plusDaysParam.setName("plusDays");
        plusDaysParam.setValue("1");
        configParam.addParam(plusDaysParam);
        Param formatParam = new Param();
        formatParam.setName("format");
        formatParam.setValue("yyyy-MM-dd");
        configParam.addParam(formatParam);
        
        assertTrue(_morpher.canMorphParameter("category", "ParamName"));
        List<Param> morphedParam = _morpher.morphParameter(configParam);
        assertEquals(1, morphedParam.size());
        assertEquals("ParamName", morphedParam.get(0).getName());
        assertEquals(new DateMidnight().plusDays(1).toString("yyyy-MM-dd"), morphedParam.get(0).getValue());
    }

    @Test
    public void plusDaysWithResultParamName() throws Exception {
        Param configParam = new Param();
        configParam.setName("ParamName");
        Param plusDaysParam = new Param();
        plusDaysParam.setName("plusDays");
        plusDaysParam.setValue("1");
        configParam.addParam(plusDaysParam);
        Param formatParam = new Param();
        formatParam.setName("format");
        formatParam.setValue("yyyy-MM-dd");
        configParam.addParam(formatParam);
        Param resultParamName = new Param();
        resultParamName.setName("resultParamName");
        resultParamName.setValue("Foo");
        configParam.addParam(resultParamName);
        
        assertTrue(_morpher.canMorphParameter("category", "ParamName"));
        List<Param> morphedParam = _morpher.morphParameter(configParam);
        assertEquals(1, morphedParam.size());
        assertEquals(new DateMidnight().plusDays(1).toString("yyyy-MM-dd"), morphedParam.get(0).getValue());
        assertEquals("Foo", morphedParam.get(0).getName());
    }

    @Test
    public void plusNegativeDays() throws Exception {
        Param configParam = new Param();
        configParam.setName("ParamName");
        Param plusDaysParam = new Param();
        plusDaysParam.setName("plusDays");
        plusDaysParam.setValue("-1");
        configParam.addParam(plusDaysParam);
        Param formatParam = new Param();
        formatParam.setName("format");
        formatParam.setValue("yyyy-MM-dd");
        configParam.addParam(formatParam);
        
        assertTrue(_morpher.canMorphParameter("category", "ParamName"));
        List<Param> morphedParam = _morpher.morphParameter(configParam);
        assertEquals(1, morphedParam.size());
        assertEquals(new DateMidnight().minusDays(1).toString("yyyy-MM-dd"), morphedParam.get(0).getValue());
    }
    
    @Test
    public void minusDays() throws Exception {
        Param configParam = new Param();
        configParam.setName("ParamName");
        Param plusDaysParam = new Param();
        plusDaysParam.setName("minusDays");
        plusDaysParam.setValue("1");
        configParam.addParam(plusDaysParam);
        Param formatParam = new Param();
        formatParam.setName("format");
        formatParam.setValue("yyyy-MM-dd");
        configParam.addParam(formatParam);
        
        assertTrue(_morpher.canMorphParameter("category", "ParamName"));
        List<Param> morphedParam = _morpher.morphParameter(configParam);
        assertEquals(1, morphedParam.size());
        assertEquals(new DateMidnight().minusDays(1).toString("yyyy-MM-dd"), morphedParam.get(0).getValue());
    }

}
