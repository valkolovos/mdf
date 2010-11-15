package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mdf.mockdata.generated.Param;
import org.testng.annotations.BeforeMethod;


public class RequestParameterMatcherMorpherTest {

    private MockDataManager _mockDataManager;

    @BeforeMethod
    public void setUp() throws Exception {
        _mockDataManager = new MockDataManager(
                "org/mdf/mockdata/RequestParameterMatcherMorpherTestData.xml");
    }

    @org.testng.annotations.Test()
    public void matchAndMorph() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("1");
        requestParam.setValue("Request Param Value");

        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(2, response.length);
        assertEquals("param1", response[0].getName());
        assertEquals("Request Param Value", response[0].getValue());
        assertEquals("param2", response[1].getName());
        assertEquals("not morphed", response[1].getValue());
    }

    @org.testng.annotations.Test()
    public void nullValue() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("test null value");
        requestParam.setValue("true");

        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(1, response.length);
        assertEquals("should be null param", response[0].getName());
        assertEquals("set null value", response[0].getValue());
    }

    @org.testng.annotations.Test()
    public void nestedValue() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("test nested param");
        requestParam.setValue("true");
        Param nestedRequestParam = new Param();
        nestedRequestParam.setName("requestParam1");
        Param childParam = new Param();
        childParam.setName("childParam");
        childParam.setValue("Nested Request Param Value");
        nestedRequestParam.addParam(childParam);

        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam, nestedRequestParam }));
        assertEquals(1, response.length);
        assertEquals("param1", response[0].getName());
        assertEquals(1, response[0].getParamCount());
        assertEquals("childParam", response[0].getParam(0).getName());
        assertEquals("Nested Request Param Value", response[0].getParam(0).getValue());
    }
    
    @org.testng.annotations.Test()
    public void regExDoesntMatch() throws Exception {
        RequestParameterMatcherMorpher rpmm = (RequestParameterMatcherMorpher)_mockDataManager.getParameterMorpher("requestParamMM");
        Param regExInitParam = new Param();
        regExInitParam.setName("param name regex");
        regExInitParam.setValue(".*foo.*");
        rpmm.setInitParams(regExInitParam);

        Param requestParam = new Param();
        requestParam.setName("1");
        requestParam.setValue("Request Param Value");

        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(2, response.length);
        assertEquals("param1", response[0].getName());
        assertEquals("requestParam: 1", response[0].getValue());
        assertEquals("param2", response[1].getName());
        assertEquals("not morphed", response[1].getValue());
    }

    @org.testng.annotations.Test()
    public void regExMatches() throws Exception {
        RequestParameterMatcherMorpher rpmm = (RequestParameterMatcherMorpher)_mockDataManager.getParameterMorpher("requestParamMM");
        Param regExInitParam = new Param();
        regExInitParam.setName("param name regex");
        regExInitParam.setValue("param\\d");
        rpmm.setInitParams(regExInitParam);

        Param requestParam = new Param();
        requestParam.setName("1");
        requestParam.setValue("Request Param Value");

        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(2, response.length);
        assertEquals("param1", response[0].getName());
        assertEquals("Request Param Value", response[0].getValue());
        assertEquals("param2", response[1].getName());
        assertEquals("not morphed", response[1].getValue());
    }

    @org.testng.annotations.Test()
    public void paramNameListContains() throws Exception {
        RequestParameterMatcherMorpher rpmm = (RequestParameterMatcherMorpher)_mockDataManager.getParameterMorpher("requestParamMM");
        Param paramNameParam = new Param();
        paramNameParam.setName("param name");
        paramNameParam.setValue("param1");
        rpmm.setInitParams(paramNameParam);

        Param requestParam = new Param();
        requestParam.setName("1");
        requestParam.setValue("Request Param Value");

        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(2, response.length);
        assertEquals("param1", response[0].getName());
        assertEquals("Request Param Value", response[0].getValue());
        assertEquals("param2", response[1].getName());
        assertEquals("not morphed", response[1].getValue());
    }
    
    @org.testng.annotations.Test()
    public void paramNameListDoesntContain() throws Exception {
        RequestParameterMatcherMorpher rpmm = (RequestParameterMatcherMorpher)_mockDataManager.getParameterMorpher("requestParamMM");
        Param paramNameParam = new Param();
        paramNameParam.setName("param name");
        paramNameParam.setValue("foo");
        rpmm.setInitParams(paramNameParam);

        Param requestParam = new Param();
        requestParam.setName("1");
        requestParam.setValue("Request Param Value");
    
        Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(2, response.length);
        assertEquals("param1", response[0].getName());
        assertEquals("requestParam: 1", response[0].getValue());
        assertEquals("param2", response[1].getName());
        assertEquals("not morphed", response[1].getValue());
    }

    @org.testng.annotations.Test()
    public void threading() throws Exception {
        final String requestVal1 = "request value 1";
        final String requestVal2 = "request value 2";

        Callable<Param[]> callable1 = new Callable<Param[]>() {

            public Param[] call() throws Exception {
                Param requestParam = new Param();
                requestParam.setName("1");
                requestParam.setValue(requestVal1);

                Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
                return response;
            }

        };

        Callable<Param[]> callable2 = new Callable<Param[]>() {

            public Param[] call() throws Exception {
                Param requestParam = new Param();
                requestParam.setName("1");
                requestParam.setValue(requestVal2);

                Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
                return response;
            }

        };

        FutureTask<Param[]> ft1 = new FutureTask<Param[]>(callable1);
        FutureTask<Param[]> ft2 = new FutureTask<Param[]>(callable2);
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 500, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        for (int i = 0; i < 1000; i++) {
            tpe.execute(ft1);
            tpe.execute(ft2);

            Param[] resp1 = ft1.get();
            assertEquals("First response param name should be \"param1\"", "param1", resp1[0].getName());
            assertEquals("First response param value should be \"request value 1\"", requestVal1, resp1[0].getValue());

            Param[] resp2 = ft2.get();
            assertEquals("First response param name should be \"param1\"", "param1", resp2[0].getName());
            assertEquals("First response param value should be \"request value 2\"", requestVal2, resp2[0].getValue());
        }
    }

}
