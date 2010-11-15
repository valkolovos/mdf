package org.mdf.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.mdf.mockdata.ParameterMatcher;
import org.mdf.mockdata.ParameterMorpher;
import org.mdf.mockdata.generated.Param;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * As a matcher, provides a way to match incoming XML requests using XPath. As a morpher, provides
 * a way to represent XML data in mock data format.<br/>
 * <br/>
 * Initialization parameter is:<br/>
 * defaultParameterName = (string) - sets the default parameter name to match<br/>
 * It is not required and defaults to "xml-response"<br/>
 * <br/>
 * Example:
 * <pre>
* &lt;mock-data&gt;
*     &lt;parameterMatcher id="xmlMatcherMorpher" class="com.orbitz.servicetests.xml.XmlParameterMatcherMorpher"/&gt;
*     &lt;parameterMorpher ref="xmlMatcherMorpher"/&gt;
*     &lt;category&gt;
*         &lt;test&gt;
*             &lt;request&gt;
*                 &lt;param name="//EngineDoc/ContentType" value="OrderFormDoc" /&gt;
*                 &lt;param name="//EngineDoc/OrderFormDoc/CreditCard/Number" value="1234567890123456" /&gt;
*             &lt;/request&gt;
*             &lt;response&gt;
*                 &lt;param name="xml-response"&gt;
*                     &lt;param name="DocVersion" value="1.0" /&gt;
*                     &lt;param name="EngineDoc"&gt;
*                         &lt;param name="ContentType" value="OrderFormDoc" /&gt;
*                         &lt;param name="IPAddress" value="127.0.0.1" /&gt;
*                         &lt;param name="Total" value="00"&gt;
*                              &lt;param name="xml attributes"&gt;
*                                   &lt;param name="DataType" value="Money" /&gt;
*                                   &lt;param name="Currency" value="840" /&gt;
*                              &lt;/param&gt;
*                         &lt;/param&gt;
*                     &lt;/param&gt;
*                 &lt;/param&gt;
*             &lt;/response&gt;
*         &lt;/test&gt;
*     &lt;/category&gt;
* &lt;/mock-data&gt;
* </pre>
* Will match the following request:
* <pre>
* &lt;foo&gt;
*   &lt;DocVersion&gt;1.0&lt;/DocVersion&gt;
*   &lt;EngineDoc&gt;
*     &lt;ContentType&gt;OrderFormDoc&lt;/ContentType&gt;
*     &lt;OrderFormDoc&gt;
*       &lt;CreditCard&gt;
*         &lt;Number&gt;1234567890123456&lt;/Number&gt;
*       &lt;/CreditCard&gt;
*     &lt;/OrderFormDoc&gt;
*   &lt;/EngineDoc&gt;
* &lt;/foo&gt;
* </pre>
* and return a parameter named "xml" that has, as its value, the following xml content:
* <pre>
* &lt;?xml version="1.0" encoding="UTF-8"?&gt;
* &lt;xml-response&gt;
*     &lt;DocVersion&gt;1.0&lt;/DocVersion&gt;
*     &lt;EngineDoc&gt;
*         &lt;ContentType&gt;OrderFormDoc&lt;/ContentType&gt;
*         &lt;IPAddress&gt;127.0.0.1&lt;/IPAddress&gt;
*         &lt;Totals&gt;
*             &lt;Total Currency="840" DataType="Money"&gt;00&lt;/Total&gt;
*         &lt;/Totals&gt;
*     &lt;/EngineDoc&gt;
* &lt;/xml-response&gt;
* </pre>
*/
public class XmlParameterMatcherMorpher implements ParameterMatcher, ParameterMorpher {

    XPath xpath = XPathFactory.newInstance().newXPath();
    String _paramName = "xml-response";

    public void setInitParams(Param... initParams) {
        for (Param p : initParams) {
            if ("defaultParameterName".equals(p.getName())) {
                _paramName = p.getValue();
            }
        }
    }

    public boolean canMorphParameter(String categoryName, String parameterName) {
        return _paramName.equals(parameterName);
    }

    public List<Param> morphParameter(Param param) throws Exception {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = createElement(doc, param);
        doc.appendChild(root);
        OutputFormat format = new OutputFormat(doc);
        StringWriter stringOut = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(stringOut, format);
        serializer.serialize(doc);
        Param responseParam = new Param();
        responseParam.setName("xml response");
        responseParam.setValue(stringOut.toString());
        return Arrays.asList(new Param[] { responseParam });
    }

    public boolean paramsMatch(List<Param> requestParams, Param[] testParams) {
        for (Param testParam : testParams) {
            XPathExpression expression = null;
            try {
                expression = xpath.compile(testParam.getName());
                String xmlValue = expression
                        .evaluate(new InputSource(new StringReader(requestParams.get(0).getValue())));
                if (!testParam.getValue().equals(xmlValue)) {
                    return false;
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private Element createElement(Document doc, Param param) {
        Element element = null;
        try {
            element = doc.createElement(param.getName());
        } catch (DOMException e) {
            System.out.println(param.getName());
            throw e;
        }
        List<Param> nonAttributeParams = new ArrayList<Param>();
        for (Param child : param.getParam()) {
            if (child.getName().equals("xml attributes")) {
                for (Param attribute : child.getParam()) {
                    Attr attr = doc.createAttribute(attribute.getName());
                    attr.setValue(attribute.getValue());
                    element.setAttributeNode(attr);
                }
            } else {
                nonAttributeParams.add(child);
            }
        }
        for (Param nonAttribParam : nonAttributeParams) {
            element.appendChild(createElement(doc, nonAttribParam));
        }
        if (param.getValue() != null) {
            element.appendChild(doc.createTextNode(param.getValue()));
        }
        return element;
    }

}
