package org.mdf.mockdata.reflection;

import java.io.IOException;
import java.io.Reader;

import org.mdf.mockdata.generated.Param;

public class MockDataReader extends Reader {
    
    private Param _objectParam;
    
    public MockDataReader(Param objectParam) {
        _objectParam = objectParam;
    }

    public void close() throws IOException {
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        return 0;
    }
    
    public Param getObjectParam() {
        return _objectParam;
    }

}
