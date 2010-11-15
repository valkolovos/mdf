package org.mdf.mockdata.reflection;

import java.io.IOException;
import java.io.Writer;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;
import org.mdf.mockdata.ReflectionParamBuilderUtil3;
import org.mdf.mockdata.generated.Param;

import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.AbstractXmlWriter;

public class MockDataXmlWriter extends AbstractXmlWriter {
    private UnsynchronizedStack<Param> _paramStack = new UnsynchronizedStack<Param>();
    private Writer _writer;
    private Marshaller _marshaller;

    public MockDataXmlWriter() {
        XMLContext context = new XMLContext();
        context.setProperty("org.exolab.castor.indent", true);
        _marshaller = context.createMarshaller();
        _marshaller.setSuppressNamespaces(true);
    }
    
    void setWriter(Writer writer) {
        _writer = writer;
        _paramStack.clear();
    }

    public Param getParam() {
        return _paramStack.get(0);
    }

    public void startNode(String name) {
        Param p = new Param();
        p.setName(escapeXmlName(name));
        if (!_paramStack.isEmpty()) {
            Param parentParam = _paramStack.peek();
            parentParam.addParam(p);
        }
        _paramStack.push(p);
    }

    public void setValue(String text) {
        _paramStack.peek().setValue(text);
    }

    public void flush() {
        try {
            _marshaller.setWriter(_writer);
            _marshaller.marshal(_paramStack.get(0));
            _writer.flush();
        } catch (Exception e) {
            throw new StreamException(e);
        }
    }

    public void endNode() {
        if (_paramStack.size() > 1) {
            _paramStack.pop();
        }
    }

    public void close() {
        try {
            _writer.close();
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    public void addAttribute(String name, String value) {
        Param currentParam = _paramStack.peek();
        Param attributeParam = new Param();
        attributeParam.setName(ReflectionParamBuilderUtil3.XSTREAM_ATTRIBUTE + escapeXmlName(name));
        attributeParam.setValue(value);
        currentParam.addParam(attributeParam);
    }
}
