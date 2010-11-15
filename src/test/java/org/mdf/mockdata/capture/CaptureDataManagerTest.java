package org.mdf.mockdata.capture;

import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CaptureDataManagerTest {

    private CaptureDataManager _captureDataManager;

    @BeforeMethod
    protected void setUp() throws Exception {
        _captureDataManager = new CaptureDataManager();
    }

    @Test()
    public void testInitiallyHasACategory() {
        assertNotNull(_captureDataManager.getMockData().getCategory()[0]);
    }
    
    @Test()
    public void testAddTest() {
        org.mdf.mockdata.generated.Test test = new org.mdf.mockdata.generated.Test();
        _captureDataManager.addTest(test);
        assertNotNull(_captureDataManager.getMockData().getCategory()[0].getTest());
    }
}
