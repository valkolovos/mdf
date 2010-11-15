package org.mdf.mockdata.capture;

import static org.testng.AssertJUnit.assertEquals;

import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.testng.annotations.Test;

public class CaptureUtilsTest {
    
    @Test
    public void testCreateTest() throws Exception {
        Object[] args = new Object[]{"bar"};
        Object result = new Integer(3);
        org.mdf.mockdata.generated.Test test = CaptureUtils.createTest(FooService.class.getMethod("getFoo", String.class), args, result);
        
        Request request = test.getRequest();
        Param firstReqParam = request.getParam(0);
        assertEquals("serviceInterface", firstReqParam.getName()); 
        assertEquals("org.mdf.mockdata.capture.CaptureUtilsTest$FooService", firstReqParam.getValue());
        Param secondReqParam = request.getParam(1);
        assertEquals("methodName", secondReqParam.getName()); 
        assertEquals("getFoo", secondReqParam.getValue());
        Param thirdReqParam = request.getParam(2);
        assertEquals("arg1", thirdReqParam.getName()); 
        assertEquals("bar", thirdReqParam.getValue());
        Response response = test.getResponse();
        Param responseParam = response.getParam(0);
        assertEquals("java.lang.Integer", responseParam.getName());
        assertEquals("3", responseParam.getValue());
    }
    
    interface FooService {
        public Integer getFoo(String bar);
    }
}
