package org.mdf.mockdata.remote;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


public interface SimulatorService {
    public void setMockData(String fileName) throws Exception;
    
    public void doRequest(Map<String, String[]> requestParams, InputStream is, OutputStream os) throws Exception;
}
