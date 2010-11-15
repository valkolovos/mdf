package org.mdf.xml;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.generated.Param;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;

public class XmlParameterMorpherTest {

    @Test()
    public void testParamsMatch() throws Exception {
        MockDataManager manager = new MockDataManager("org/mdf/xml/XmlParameterMorpherTestData.xml");
        Param requestParam = new Param();
        requestParam.setName("xml");
        StringBuilder sb = new StringBuilder("<foo>");
        sb.append("  <DocVersion>1.0</DocVersion>");
        sb.append("  <EngineDoc>");
        sb.append("    <ContentType>OrderFormDoc</ContentType>");
        sb.append("    <OrderFormDoc>");
        sb.append("      <Consumer>");
        sb.append("        <PaymentMech>");
        sb.append("          <CreditCard>");
        sb.append("            <Number>5200000000000007</Number>");
        sb.append("          </CreditCard>");
        sb.append("        </PaymentMech>");
        sb.append("      </Consumer>");
        sb.append("    </OrderFormDoc>");
        sb.append("    <User>");
        sb.append("      <Name>orbitz_dev_user</Name>");
        sb.append("      <Password>orbitz123</Password>");
        sb.append("    </User>");
        sb.append("  </EngineDoc>");
        sb.append("</foo>");
        requestParam
                .setValue(sb.toString());
        Param[] responseParams = manager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertNotNull(responseParams);
        assertEquals(1, responseParams.length);
    }

    @Test()
    public void testMorphToXml() throws Exception {
        MockDataManager manager = new MockDataManager("org/mdf/xml/XmlParameterMorpherTestData.xml");
        Param requestParam = new Param();
        requestParam.setName("xml");
        requestParam.setValue("<test>true</test>");
        Param[] responseParams = manager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(1, responseParams.length);
        printParam(responseParams[0]);

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expression = xpath
                .compile("/xml-response/EngineDoc/OrderFormDoc/Transaction/CardProcResp/AvsRespCode");
        String xmlValue = expression.evaluate(new InputSource(new StringReader(responseParams[0].getValue())));
        assertEquals("YY", xmlValue);
    }
    private String printParam(Param objectParam) throws IOException, MarshalException, ValidationException {
        XMLContext xmlContext = new XMLContext();
        xmlContext.setProperty("org.exolab.castor.indent", true);
        Marshaller m = xmlContext.createMarshaller();
        StringWriter w = new StringWriter();
        m.setWriter(w);
        m.marshal(objectParam);
        String data = w.toString();
        System.out.println(data);
        return data;
    }

}
