package org.mdf.mockdata.reflection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;

public class MockDataXmlDriver extends AbstractXmlDriver {
    private MockDataXmlWriter _xmlWriter;
    
    public MockDataXmlDriver() {
        _xmlWriter = new MockDataXmlWriter();
    }

    public HierarchicalStreamWriter createWriter(OutputStream out) {
        return createWriter(new OutputStreamWriter(out));
    }

    public HierarchicalStreamWriter createWriter(Writer out) {
        _xmlWriter.setWriter(out);
        return _xmlWriter;
    }

    public HierarchicalStreamReader createReader(InputStream in) {
        throw new UnsupportedOperationException("Use createReader(Reader in) instead");
    }

    public HierarchicalStreamReader createReader(Reader in) {
        if (!(in instanceof MockDataReader)) {
            throw new RuntimeException();
        }
        return new MockDataXmlReader(((MockDataReader)in).getObjectParam());
    }

    public MockDataXmlWriter getWriter() {
        return _xmlWriter;
    }
}
