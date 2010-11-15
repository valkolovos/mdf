package org.mdf.mockdata.capture;

import java.io.File;

import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Test;

/**
 * This is a DataManager for capturing data to a file. It can be wired into multiple capture advice classes
 * and will capture all the data into a single file.
 * <P>
 * To extract the data, call the writeTestData() method. One way to do this is to add a "destroy-method" attribute
 * to the bean definition to cause it to write the data at the time of application shutdown.
 * <P>
 * @see OLCInvocationHandlerMDFAdvice
 * @see LapsangServiceWrapperMDFAdvice
 * 
 */
public class CaptureDataManager {
    
    private MockData _mockData = new MockData();
    private String _fileName;
    private String _dumpDir;
    private boolean _validate = true;
    
    public CaptureDataManager() {
        Category category = new Category();
        category.setName("services");
        _mockData.addCategory(category);
    }
    
    public void addTest(Test test) {
        MockData testData = getMockData();
        Category category = testData.getCategory()[0];
        category.addTest(test);
    }
    
    public void writeTestData() throws Exception {
        String fileName = _fileName == null ? "CaptureDataSource.xml" : _fileName;
        String dumpDir = _dumpDir == null ? System.getProperty("orbitz.server.log.dir") : _dumpDir;
        MockDataManager.marshallTestData(getMockData(), dumpDir + File.separator + fileName, _validate);
    }

    public synchronized MockData getMockData() {
        return _mockData;
    }

    public boolean isValidate() {
        return _validate;
    }
    public void setValidate(boolean validate) {
        this._validate = validate;
    }
    public void setMockData(MockData mockData) {
        this._mockData = mockData;
    }
    public String getFileName() {
        return _fileName;
    }
    public void setFileName(String filename) {
        this._fileName = filename;
    }
    public String getDumpDir() {
        return _dumpDir;
    }
    public void setDumpDir(String dir) {
        _dumpDir = dir;
    }
    

}
