package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.mdf.mockdata.generated.Test;
import org.testng.annotations.BeforeMethod;


public class InsertedDataParameterMatcherMorpherTest {
    
    private MockDataManager _mockDataManager;
    
    @BeforeMethod
    public void setUp() throws Exception {
        _mockDataManager = new MockDataManager("org/mdf/mockdata/InsertedDataParameterMatcherMorpherTestData.xml");
    }
    
    @org.testng.annotations.Test()
    public void testMatchAndMorph() throws Exception {
        Param requestParam = new Param();
        requestParam.setName("1");
        requestParam.setValue("column value");
        
        Param[] responseParams = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("No response, since no data captured yet", 0, responseParams.length);
        
        Test t = new Test();
        Request req = new Request();
        Response resp = new Response();
        Param responseParam = new Param();
        responseParam.setName("captured data response");
        responseParam.setValue("");
        resp.addParam(responseParam);
        t.setRequest(req);
        t.setResponse(resp);
        
        Param capturedParam = new Param();
        capturedParam.setName("COLUMN_1");
        capturedParam.setValue("column value");
        req.addParam(capturedParam);
        capturedParam = new Param();
        capturedParam.setName("COLUMN_2");
        capturedParam.setValue("column 2 value");
        req.addParam(capturedParam);
        
        _mockDataManager.addCapturedData("TABLE_1", t);
        responseParams = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Data captured. Should get 2 param back", 2, responseParams.length);
        assertEquals("Response param name should be \"TABLE_1.COLUMN_1\"", "TABLE_1.COLUMN_1", responseParams[0].getName());
        assertEquals("Response value should be \"column value\"", "column value", responseParams[0].getValue());
        assertEquals("Response param name should be \"aliased_name\"", "aliased_name", responseParams[1].getName());
        assertEquals("Response value should be \"column 2 value\"", "column 2 value", responseParams[1].getValue());
    }

    @org.testng.annotations.Test()
    public void testThreading() throws Exception {
        final String colVal1 = "column value 1";
        final String colVal2 = "column value 2";
        
        Callable<Param[]> callable1 = new Callable<Param[]>() {

            public Param[] call() throws Exception {
                Param requestParam = new Param();
                requestParam.setName("1");
                requestParam.setValue(colVal1);

                Test t = new Test();
                Request req = new Request();
                Response resp = new Response();
                Param responseParam = new Param();
                responseParam.setName("captured data response");
                responseParam.setValue("");
                resp.addParam(responseParam);
                t.setRequest(req);
                t.setResponse(resp);
                
                Param capturedParam = new Param();
                capturedParam.setName("COLUMN_1");
                capturedParam.setValue(colVal1);
                req.addParam(capturedParam);
                
                _mockDataManager.addCapturedData("TABLE_1", t);
                Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
                return response;
            }
            
        };
        
        Callable<Param[]> callable2 = new Callable<Param[]>() {

            public Param[] call() throws Exception {
                Param requestParam = new Param();
                requestParam.setName("1");
                requestParam.setValue(colVal2);

                Test t = new Test();
                Request req = new Request();
                Response resp = new Response();
                Param responseParam = new Param();
                responseParam.setName("captured data response");
                responseParam.setValue("");
                resp.addParam(responseParam);
                t.setRequest(req);
                t.setResponse(resp);
                
                Param capturedParam = new Param();
                capturedParam.setName("COLUMN_1");
                capturedParam.setValue(colVal2);
                req.addParam(capturedParam);
                
                _mockDataManager.addCapturedData("TABLE_1", t);
                Param[] response = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
                return response;
            }
            
        };
        
        FutureTask<Param[]> ft1 = new FutureTask<Param[]>(callable1);
        FutureTask<Param[]> ft2 = new FutureTask<Param[]>(callable2);
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2, 500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1));
        
        tpe.execute(ft1);
        tpe.execute(ft2);
        
        Param[] resp1 = ft1.get();
        assertEquals("Data captured. Should get 2 param back", 2, resp1.length);
        assertEquals("Response param name should be \"TABLE_1.COLUMN_1\"", "TABLE_1.COLUMN_1", resp1[0].getName());
        assertEquals("Response value should be \"column value\"", colVal1, resp1[0].getValue());
        assertNull("Response row 2 value should be null", resp1[1].getValue());

        Param[] resp2 = ft2.get();
        assertEquals("Data captured. Should get 2 param back", 2, resp2.length);
        assertEquals("Response param name should be \"TABLE_1.COLUMN_1\"", "TABLE_1.COLUMN_1", resp2[0].getName());
        assertEquals("Response value should be \"column value\"", colVal2, resp2[0].getValue());
    }
    
    static class C {
        private ThreadLocal<String[]> tl = new ThreadLocal<String[]>() {
            private String[] _key;
            public String[] get() {
                return _key;
            }
            public void set(String[] key) {
                _key = key;
            }
        };
        
        public C(String s1, String s2, String s3) {
            tl.set(new String[] { s1, s2, s3 });
        }
        
        public void foo() {
            System.out.println(tl.get()[0] + " " + tl.get()[1] + " " + tl.get()[2]);
        }
    }
    
    @org.testng.annotations.Test()
    public void testGeneralInsertMatch() throws Exception
    {
    	Param requestParam = new Param();
        requestParam.setName("1");
        requestParam.setValue("HOUSE");
        
        Param[] responseParams = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Resp", responseParams[0].getName());
        
        Test t = new Test();
        Request req = new Request();
        Response resp = new Response();
        Param responseParam = new Param();
        responseParam.setName("captured data response");
        responseParam.setValue("");
        resp.addParam(responseParam);
        t.setRequest(req);
        t.setResponse(resp);
        
        Param capturedParam = new Param();
        capturedParam.setName("COLUMN_1");
        capturedParam.setValue("column value");
        req.addParam(capturedParam);
        capturedParam = new Param();
        capturedParam.setName("COLUMN_2");
        capturedParam.setValue("FOO");
        req.addParam(capturedParam);
        
        _mockDataManager.addCapturedData("TABLE_2", t);
        responseParams = _mockDataManager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals("Data captured. Should get 1 param back", 1, responseParams.length);
        assertEquals("Response param name should be \"TABLE_1.COLUMN_1\"", "New", responseParams[0].getName());
        
    }
    
}
