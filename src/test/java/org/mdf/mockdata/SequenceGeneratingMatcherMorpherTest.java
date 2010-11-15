package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.mdf.mockdata.generated.Param;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class SequenceGeneratingMatcherMorpherTest {
    
    private MockDataManager _mockDataManager;
    
    @BeforeMethod
    public void setUp() throws Exception {
        _mockDataManager = new MockDataManager("org/mdf/mockdata/SequenceGeneratingMatcherMorpherTestData.xml");
    }
    
    @Test()
    public void testMorph() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("foo");
        requestParam.setValue("bar");
        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Got one response param", 1, response.length);
        assertEquals("Param name is \"nextval\"", "nextval", response[0].getName());
        assertEquals("Param value is 1", "1", response[0].getValue());
        response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Param value is 2", "2", response[0].getValue());
    }

    @Test()
    public void testMatch() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("1");
        requestParam.setValue("1");
        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Haven't actually set a sequence yet, won't match", 0, response.length);
        
        // setting sequence
        requestParam = new Param();
        requestParam.setName("foo");
        requestParam.setValue("bar");
        _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        
        requestParam.setName("1");
        requestParam.setValue("1");
        response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Sequence now incremented. Should match", 1, response.length);
        assertEquals("Response param name is \"response\"", "response", response[0].getName());
        assertEquals("Response param value is \"response balue\"", "response value", response[0].getValue());
    }
    
    @Test()
    public void testMorphWithInit() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("house");
        requestParam.setValue("corn");
        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Got one response param", 1, response.length);
        assertEquals("Param name is \"nextval\"", "nextval", response[0].getName());
        assertEquals("Param value is 5", "5", response[0].getValue());
        response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Param value is 6", "6", response[0].getValue());
    }
    
}
