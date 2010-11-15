package org.mdf.xml;

import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.ParameterMorpher;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


public class XmlCaptureUtilsTest {

    @Test()
    public void testMorphToXml() throws Exception {
        MockDataManager manager = new MockDataManager();
        // If anyone wonders, this was from IVR. Pulled it because it inspired me to write this.
        String xml = "<?xml version=\"1.0\"?><vxml version=\"2.1\" application=\"/ivr/service/wl2/app_root.vxml\">"
			+ "<form id=\"initForm\"><block><assign name=\"POSCODE\" expr=\"\'ORB\'\"/><assign name=\"LANG_CODE\" expr=\"\'en\'\"/>"
			+ "<assign name=\"COUNTRY_CODE\" expr=\"\'US\'\"/>"
			+ "<assign name=\"ERROR_AUDIO_FILE\" expr=\"'/error/v1/requested.wav'\"/>"
			+ "<assign name=\"ERROR_AUDIO_TTS\" expr=\"'Thank  you for calling Orbitz. Our automated phone system is currently  unavailable. Please hold and your call will be transferred.'\"/>"
			+ "<assign name=\"EMERGENCY_NUMBER\" expr=\"'0031205126438'\"/>"
			+ "<assign name=\"AUDIO_BASE_URL_NO_LANG\"  expr=\"'http://audio.en-US.tellme.com/orbitz/redesigned-phaseI/ORB/en_US'\"/>"
			+ "<assign name=\"DATE_FORMAT\" expr=\"'the|[ordinal]|of|[month]'\"/>"
			+ "<assign name=\"IS_PIN_CHECK\" expr=\"'false'\"/>"
			+ "<assign name=\"IS_AIR_PROMO\" expr=\"'true'\"/>"
			+ "<assign name=\"IS_EMERGENCY_FLAG\" expr=\"'false'\"/>"
			+ "<assign name=\"IS_SALES_LOGIN\" expr=\"'true'\"/>"
			+ "<assign name=\"DEFAULT_ROUTE\" expr=\"'8004486523'\"/>"
			+ "<assign name=\"AUDIO_BASE_URL\" expr=\"AUDIO_BASE_URL_NO_LANG + '/' + LANG_CODE\"/>"
			+ "<assign name=\"LOCALE\" expr=\"LANG_CODE + '_' + COUNTRY_CODE\"/>"
			+ "<assign name=\"DEFAULT_LOCALE\" expr=\"LANG_CODE + '_' + COUNTRY_CODE\"/>"
			+ "<script>erma_history_add(\"GREETING\")</script>"
			+ "<script>history_add(\"NEW_CALL\")</script>"
			+ "<goto next=\"/ivr/service/wl2/greeting.vxml\"/>"
			+ "</block>" + "</form>" + "</vxml>";
        List<Param> params = new ArrayList<Param>();
        Document doc = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder()
			.parse(new ByteArrayInputStream(xml.getBytes()));
        
        params =XmlCaptureUtils.documentToParams(doc);
        
        MockData testData=new MockData();
        Category vCategory = new Category();
        testData.addCategory(vCategory);
        org.mdf.mockdata.generated.Test vTest = new org.mdf.mockdata.generated.Test();
        vCategory.addTest(vTest);
        vTest.setRequest(new Request());
        Response vResponse = new Response();
        vTest.setResponse(vResponse);
        Param responseParam = new Param();
        responseParam.setName("xml-response");
        responseParam.setParam(params.toArray(new Param[0]));
        vResponse.addParam(responseParam);
        ArrayList<ParameterMorpher> morphers = new ArrayList<ParameterMorpher>();
        ParameterMorpher xmlParamMorpher = new ParameterMorpher();
        xmlParamMorpher.setClazz("org.mdf.xml.XmlParameterMatcherMorpher");
        morphers.add(xmlParamMorpher);
        testData.setParameterMorpher(morphers.toArray(new ParameterMorpher[0]));
        Param requestParam = new Param();
        requestParam.setName("xml");
        requestParam.setValue("<test>true</test>");
        manager.setMockData(testData);
        Param[] responseParams = manager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(1, responseParams.length);
        printParam(responseParams[0]);

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expression = xpath
                .compile("//assign[1]/@expr");
        String xmlValue = expression.evaluate(new InputSource(new StringReader(responseParams[0].getValue())));
        assertEquals("'ORB'", xmlValue);
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
