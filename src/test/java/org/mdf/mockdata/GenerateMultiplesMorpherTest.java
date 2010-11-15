package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Arrays;

import org.mdf.mockdata.generated.Param;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class GenerateMultiplesMorpherTest {
    private MockDataManager _mdm;
    
    @BeforeClass
    void setup() throws Exception {
        _mdm = new MockDataManager("org/mdf/mockdata/GenerateMultiplesMorpherTestData.xml");
    }
    
    @Test
    public void happyPath() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("testName");
        requestParam.setValue("happyPath");
        Param[] response = _mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertNotNull(response);
        assertEquals(4, response.length);
        for (int i = 0; i < response.length; i++) {
            assertEquals("data", response[0].getName());
            assertEquals("foo", response[0].getValue());
        }
    }
    
    @Test
    public void nullChildParams() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("testName");
        requestParam.setValue("nullChildParams");
        Param[] response = _mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertNotNull(response);
        assertEquals(0, response.length);
    }
    
    @Test
    public void noNumberParam() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("testName");
        requestParam.setValue("noNumberParam");
        Param[] response = _mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertNotNull(response);
        assertEquals(0, response.length);
    }
    
    @Test
    public void noDataParam() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("testName");
        requestParam.setValue("noDataParam");
        Param[] response = _mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertNotNull(response);
        assertEquals(0, response.length);
    }
    
    @Test
    public void multipleParamHasNoChildren() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("testName");
        requestParam.setValue("dataParamHasSameNameAsMultipleParam");
        Param[] response = _mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(4, response.length);
        for (int i = 0; i < response.length; i++) {
            assertEquals("data", response[i].getName());
            assertEquals("foo", response[i].getValue());
        }
    }
    
    @Test
    public void stringForMultiplesParamValue() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("testName");
        requestParam.setValue("stringForMultiplesParamValue");
        Param[] response = _mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertNotNull(response);
        assertEquals(0, response.length);
    }
    
    @Test
    public void floatForMultiplesParamValue() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("testName");
        requestParam.setValue("floatForMultiplesParamValue");
        Param[] response = _mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertNotNull(response);
        assertEquals(0, response.length);
    }

}
