package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.StringReader;
import java.util.Arrays;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.testng.annotations.Test;


public class RegExMatcherTest {
    @Test
    public void match() throws Exception {
        MockDataManager mdm = new MockDataManager("org/mdf/mockdata/RegExMatcherTestData.xml");
        Param requestParam = new Param();
        requestParam.setName("path");
        requestParam.setValue("/doSomething/unecessary/necessaryArgument/unecessary/anotherNecessaryArgument");
        Param[] response = mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(1, response.length);

        requestParam.setValue("/doSomething/different/necessaryArgument/unecessary/anotherNecessaryArgument");
        response = mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(1, response.length);
    }

    @Test
    public void noMatch() throws Exception {
        MockDataManager mdm = new MockDataManager("org/mdf/mockdata/RegExMatcherTestData.xml");
        Param requestParam = new Param();
        requestParam.setName("path");
        requestParam.setValue("/doSomething/unecessary/wrongArgument/unecessary/anotherNecessaryArgument");
        Param[] response = mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(0, response.length);

        requestParam.setValue("/doSomething/tooShort/necessaryArgument");
        response = mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(0, response.length);
    }

    @Test
    public void invalidConfig() throws Exception {
        String invalidConfigXml = "<mock-data xmlns=\"http://www.orbitz.com/schema/mock-data\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.orbitz.com/schema/mock-data ../../../../../../../src/conf/MockTestData.xsd \">"
                + "<parameterMatcher class=\"org.mdf.mockdata.RegExMatcher\"/>" + "</mock-data>";
        XMLContext context = new XMLContext();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setClass(MockData.class);
        MockData data = (MockData) unmarshaller.unmarshal(new StringReader(invalidConfigXml));
        Exception ex = null;
        try {
            new MockDataManager(data);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        assertEquals(
                "Unable to initialize matchers and morphers : init parameter \"defaultParameterName\" not specified for RegExMatcher",
                ex.getMessage());
    }
    
    @Test
    public void hotelEbieMatch() throws Exception {
        MockDataManager mdm = new MockDataManager("org/mdf/mockdata/RegExMatcherTestData.xml");
        Param requestParam = new Param();
        requestParam.setName("path");
        requestParam.setValue("/market/resolution/EBIE/1/HOT/SRCH");
        Param[] response = mdm.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(1, response.length);
    }
}
