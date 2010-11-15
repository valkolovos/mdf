package org.mdf.mockdata.reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mdf.mockdata.ReflectionParamBuilderUtil3;
import org.mdf.mockdata.generated.Param;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.AbstractXmlReader;

public class MockDataXmlReader extends AbstractXmlReader {
    private UnsynchronizedStack<Param> _paramStack = new UnsynchronizedStack<Param>();
    private UnsynchronizedStack<Map<String, String>> _attributeMapStack = new UnsynchronizedStack<Map<String, String>>();
    private UnsynchronizedStack<Map<Integer, Param>> _attributeIndexStack = new UnsynchronizedStack<Map<Integer, Param>>();
    private UnsynchronizedStack<List<Param>> _childParamStack = new UnsynchronizedStack<List<Param>>();

    public MockDataXmlReader(Param objectParam) {
        _paramStack.push(objectParam);
        getAttributeParams(objectParam);
    }

    public void appendErrors(ErrorWriter errorWriter) {
    }

    public void close() {
    }

    public String getAttribute(String name) {
        String attribute = null;
        Map<String, String> attributeParam = _attributeMapStack.peek();
        if (attributeParam != null) {
            attribute = attributeParam.get(name);
        }
        return attribute;
    }

    public String getAttribute(int index) {
        String attribute = null;
        Map<Integer, Param> attributeParam = _attributeIndexStack.peek();
        if (attributeParam != null) {
            attribute = attributeParam.get(index).getValue();
        }
        return attribute;
    }

    public int getAttributeCount() {
        Map<String, String> attributeParam = _attributeMapStack.peek();
        int i = attributeParam != null ? attributeParam.size() : 0;
        return i;
    }

    public String getAttributeName(int index) {
        String attribute = null;
        Map<Integer, Param> attributeParam = _attributeIndexStack.peek();
        if (attributeParam != null) {
            attribute = attributeParam.get(index).getName().substring(
                    ReflectionParamBuilderUtil3.XSTREAM_ATTRIBUTE.length());
        }
        return unescapeXmlName(attribute);
    }

    @SuppressWarnings("unchecked")
    public Iterator getAttributeNames() {
        List<String> names = new ArrayList<String>();
        Map<String, String> attributeParam = _attributeMapStack.peek();
        if (attributeParam != null) {
            names.addAll(attributeParam.keySet());
        }
        return names.iterator();
    }

    public String getNodeName() {
        String s = _paramStack.peek().getName();
        return unescapeXmlName(s);
    }

    public String getValue() {
        String s = _paramStack.peek().getValue();
        return s;
    }

    public boolean hasMoreChildren() {
        return (!_childParamStack.peek().isEmpty());
    }

    public void moveDown() {
        Param nextChildParam = _childParamStack.peek().remove(0);
        _paramStack.push(nextChildParam);
        getAttributeParams(nextChildParam);
    }

    public void moveUp() {
        _paramStack.pop();
        _attributeMapStack.pop();
        _attributeIndexStack.pop();
        _childParamStack.pop();
    }

    public HierarchicalStreamReader underlyingReader() {
        return this;
    }

    private void getAttributeParams(Param p) {
        Map<String, String> attributeParam = new HashMap<String, String>();
        Map<Integer, Param> attributeIndexMap = new HashMap<Integer, Param>();
        List<Param> childParams = new LinkedList<Param>();
        _childParamStack.push(childParams);
        for (Param child : p.getParam()) {
            if (child.getName().startsWith(ReflectionParamBuilderUtil3.XSTREAM_ATTRIBUTE)) {
                attributeParam.put(child.getName().substring(ReflectionParamBuilderUtil3.XSTREAM_ATTRIBUTE.length()),
                        child.getValue());
                attributeIndexMap.put(attributeIndexMap.size() + 1, child);
            } else {
                childParams.add(child);
            }
        }
        _attributeMapStack.push(attributeParam);
        _attributeIndexStack.push(attributeIndexMap);
    }

}
