package org.mdf.mockdata.remote;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.generated.Param;

public class GenericSimulatorServiceImpl implements SimulatorService {
    
    private MockDataManager _mdm;

    @SuppressWarnings("unchecked")
    public void doRequest(Map<String, String[]> requestParams, InputStream is, OutputStream os) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(is);
        List<Param> requestParamList = (List<Param>) ois.readObject();
        Param[] response = _mdm.findResponse(requestParamList);
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(response);
        oos.close();
    }
    
    public void setMockData(String fileName) throws Exception {
        _mdm.loadTestDataFromFile(fileName);
    }

    public void setMockDataManager(MockDataManager mdm) {
        _mdm = mdm;
    }
    
}
