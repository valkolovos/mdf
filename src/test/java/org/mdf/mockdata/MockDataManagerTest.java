package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;
import org.joda.time.DateMidnight;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.testng.annotations.Test;

public class MockDataManagerTest {

    @Test()
    public void testFindResponseListOfParam() throws Exception {
        StringBuilder mockDataString = new StringBuilder();
        mockDataString.append("<mock-data>");
        mockDataString.append("  <category>");
        mockDataString.append("    <test>");
        mockDataString.append("      <request>");
        mockDataString.append("        <param name=\"request_param1\" value=\"foo\"/>");
        mockDataString.append("      </request>");
        mockDataString.append("      <response>");
        mockDataString.append("        <param name=\"response_param1\" value=\"foo\"/>");
        mockDataString.append("      </response>");
        mockDataString.append("    </test>");
        mockDataString.append("  </category>");
        mockDataString.append("</mock-data>");
        MockData mockData = unmarshallTestData(mockDataString.toString());
        MockDataManager manager = new MockDataManager(mockData);
        Param requestParam = new Param();
        requestParam.setName("request_param1");
        requestParam.setValue("foo");
        List<Param> requestParamList = new ArrayList<Param>();
        requestParamList.add(requestParam);
        Param[] response = manager.findResponse(requestParamList);
        assertEquals(1, response.length);

        requestParam = new Param();
        requestParam.setName("request_param1");
        requestParam.setValue("bar");
        requestParamList.add(requestParam);
        response = manager.findResponse(requestParamList);
        assertEquals(1, response.length);
    }

    @Test()
    public void testLoadTestDataFromFile() throws Exception {
        MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        MockData mockData = manager.getTestData();
        assertNotNull(mockData);
        assertNull("MockData description is null", mockData.getDescription());
        assertEquals("Got 2 categories", 2, mockData.getCategoryCount());
        assertNull("Category description is null", mockData.getCategory(0).getDescription());
        assertEquals("Got 6 tests", 6, mockData.getCategory(0).getTestCount());
        org.mdf.mockdata.generated.Test templatedTest = mockData.getCategory(0).getTest(1);
        assertEquals("Templated test has 5 response parameters", 5, templatedTest.getResponse().getParamCount());
        assertEquals("Templated test parameter value is correct", "templated param", templatedTest.getResponse()
                .getParam(1).getParam(0).getValue());
        assertEquals("Replaced child params exist", 1, templatedTest.getResponse().getParam(2)
                .getParamCount());
        assertEquals("Replaced child params have correct value", "overridden child value", templatedTest.getResponse()
                .getParam(2).getParam(0).getValue());
        assertEquals("Templated test parameter value is correct", "new param - child param", templatedTest
                .getResponse().getParam(3).getParam(0).getValue());
        org.mdf.mockdata.generated.Test templatedChildParamTest = mockData.getCategory(0).getTest(2);
        assertEquals("Templated child param test has 2 response parameters", 2, templatedChildParamTest.getResponse()
                .getParamCount());
        Param firstReplacedParam = templatedChildParamTest.getResponse().getParam(0);
        assertEquals("first replaced param has 2 child params", 2, firstReplacedParam.getParamCount());
        assertEquals("first replaced param's first child param has been replaced", "replaced param value 1",
                firstReplacedParam.getParam(0).getValue());
        Param secondReplacedParam = templatedChildParamTest.getResponse().getParam(1);
        assertEquals("second replaced param has 2 child params", 2, secondReplacedParam
                .getParamCount());
        assertEquals("second replaced param's second child param has been replaced", "replaced param value 2",
                secondReplacedParam.getParam(1).getValue());

        org.mdf.mockdata.generated.Test templatedGrandchildParamTest = mockData.getCategory(0).getTest(3);
        Param replacedParam = templatedGrandchildParamTest.getResponse().getParam(0).getParam(1);
        assertEquals(2, replacedParam.getParamCount());
        assertEquals("param 1", replacedParam.getParam(0).getName());
    }

    @Test()
    public void testAddTestData() throws Exception {
        MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        StringBuilder mockDataString = new StringBuilder();
        mockDataString.append("<mock-data>");
        mockDataString.append("  <category name=\"foo\">");
        mockDataString.append("    <test>");
        mockDataString.append("      <request>");
        mockDataString.append("        <param name=\"request_param1\" value=\"foo\"/>");
        mockDataString.append("      </request>");
        mockDataString.append("      <response>");
        mockDataString.append("        <param name=\"response_param1\" value=\"foo\"/>");
        mockDataString.append("      </response>");
        mockDataString.append("    </test>");
        mockDataString.append("  </category>");
        mockDataString.append("</mock-data>");
        MockData mockData = unmarshallTestData(mockDataString.toString());
        manager.addTestData(mockData);

        // original category had no name, new category had name "foo" - no
        // match, add category
        assertEquals("Now have 3 categories", 3, manager.getTestData().getCategoryCount());

        manager.loadTestDataFromFile("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        manager.getTestData().getCategory(0).setName("foo");
        int originalTestCount = manager.getTestData().getCategory(0).getTestCount();
        manager.addTestData(mockData);
        // original category had name "foo", new category had name "foo" -
        // match, don't add category
        assertEquals("Only have 2 categories", 2, manager.getTestData().getCategoryCount());
        assertEquals("Have 1 more test", originalTestCount + 1, manager.getTestData().getCategory(0).getTestCount());

        mockDataString = new StringBuilder();
        mockDataString.append("<mock-data>");
        mockDataString.append("  <category>");
        mockDataString.append("    <test>");
        mockDataString.append("      <request>");
        mockDataString.append("        <param name=\"request_param1\" value=\"foo\"/>");
        mockDataString.append("      </request>");
        mockDataString.append("      <response>");
        mockDataString.append("        <param name=\"response_param1\" value=\"foo\"/>");
        mockDataString.append("      </response>");
        mockDataString.append("    </test>");
        mockDataString.append("  </category>");
        mockDataString.append("</mock-data>");
        mockData = unmarshallTestData(mockDataString.toString());
        manager.loadTestDataFromFile("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        manager.addTestData(mockData);
        // original category had no name, new category had no name, no
        // parameters for either category - match, don't add category
        assertEquals("Only have 2 categories", 2, manager.getTestData().getCategoryCount());

        mockDataString = new StringBuilder();
        mockDataString.append("<mock-data>");
        mockDataString.append("  <category name=\"not_foo\">");
        mockDataString.append("    <test>");
        mockDataString.append("      <request>");
        mockDataString.append("        <param name=\"request_param1\" value=\"foo\"/>");
        mockDataString.append("      </request>");
        mockDataString.append("      <response>");
        mockDataString.append("        <param name=\"response_param1\" value=\"foo\"/>");
        mockDataString.append("      </response>");
        mockDataString.append("    </test>");
        mockDataString.append("  </category>");
        mockDataString.append("</mock-data>");
        mockData = unmarshallTestData(mockDataString.toString());
        manager.loadTestDataFromFile("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        manager.getTestData().getCategory(0).setName("foo");
        manager.addTestData(mockData);
        // original category had name "foo", new category had name "not_foo" -
        // don't match, add category
        assertEquals("Now have 3 categories", 3, manager.getTestData().getCategoryCount());

        mockDataString = new StringBuilder();
        mockDataString.append("<mock-data>");
        mockDataString.append("  <category>");
        mockDataString.append("    <param name=\"cat_param1\" value=\"foo\"/>");
        mockDataString.append("    <test>");
        mockDataString.append("      <request>");
        mockDataString.append("        <param name=\"request_param1\" value=\"foo\"/>");
        mockDataString.append("      </request>");
        mockDataString.append("      <response>");
        mockDataString.append("        <param name=\"response_param1\" value=\"foo\"/>");
        mockDataString.append("      </response>");
        mockDataString.append("    </test>");
        mockDataString.append("  </category>");
        mockDataString.append("</mock-data>");
        mockData = unmarshallTestData(mockDataString.toString());
        manager.loadTestDataFromFile("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        manager.addTestData(mockData);
        // original category had no name, new category had no name,
        // original category had no parameters, new category has parameters
        // no match, add category
        assertEquals("Now have 3 categories", 3, manager.getTestData().getCategoryCount());

        mockDataString = new StringBuilder();
        mockDataString.append("<mock-data>");
        mockDataString.append("  <category>");
        mockDataString.append("    <test>");
        mockDataString.append("      <request>");
        mockDataString.append("        <param name=\"request_param1\" value=\"foo\"/>");
        mockDataString.append("      </request>");
        mockDataString.append("      <response>");
        mockDataString.append("        <param name=\"response_param1\" value=\"foo\"/>");
        mockDataString.append("      </response>");
        mockDataString.append("    </test>");
        mockDataString.append("  </category>");
        mockDataString.append("</mock-data>");
        mockData = unmarshallTestData(mockDataString.toString());
        manager.loadTestDataFromFile("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        Param catParam = new Param();
        catParam.setName("cat_param1");
        catParam.setValue("foo");
        manager.getTestData().getCategory(0).addParam(catParam);
        manager.addTestData(mockData);
        // original category had no name, new category had no name,
        // original category had parameters, new category had no parameters
        // no match, add category
        assertEquals("Now have 3 categories", 3, manager.getTestData().getCategoryCount());

        mockDataString = new StringBuilder();
        mockDataString.append("<mock-data>");
        mockDataString.append("  <category>");
        mockDataString.append("    <param name=\"cat_param1\" value=\"foo\"/>");
        mockDataString.append("    <test>");
        mockDataString.append("      <request>");
        mockDataString.append("        <param name=\"request_param1\" value=\"foo\"/>");
        mockDataString.append("      </request>");
        mockDataString.append("      <response>");
        mockDataString.append("        <param name=\"response_param1\" value=\"foo\"/>");
        mockDataString.append("      </response>");
        mockDataString.append("    </test>");
        mockDataString.append("  </category>");
        mockDataString.append("</mock-data>");
        mockData = unmarshallTestData(mockDataString.toString());
        manager.loadTestDataFromFile("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        catParam = new Param();
        catParam.setName("cat_param1");
        catParam.setValue("foo");
        manager.getTestData().getCategory(0).addParam(catParam);
        manager.addTestData(mockData);
        // original category had no name, new category had no name,
        // original category had parameters, new category had parameters
        // match, don't add category
        assertEquals("Only have 2 categories", 2, manager.getTestData().getCategoryCount());

        mockDataString = new StringBuilder();
        mockDataString.append("<mock-data>");
        mockDataString.append("  <category>");
        mockDataString.append("    <param name=\"cat_param1\" value=\"foo\"/>");
        mockDataString.append("    <test>");
        mockDataString.append("      <request>");
        mockDataString.append("        <param name=\"request_param1\" value=\"foo\"/>");
        mockDataString.append("      </request>");
        mockDataString.append("      <response>");
        mockDataString.append("        <param name=\"response\">");
        mockDataString.append("					<use-template name=\"ComplexClass-template\">");
        mockDataString.append("						<param name=\"booleanList\">");
        mockDataString.append("								<param name=\"iterableEntry\" value=\"false\"/>");
        mockDataString.append("								<param name=\"iterableEntry\" value=\"false\"/>");
        mockDataString.append("								<param name=\"iterableEntry\" value=\"false\"/>");
        mockDataString.append("						</param>");
        mockDataString.append("      				<param name=\"child\">");
        mockDataString.append("								<use-template name=\"child-template\">");
        mockDataString.append("									<param name=\"id\" value=\"1\"/>");
        mockDataString.append("								</use-template>");
        mockDataString.append("						</param>");
        mockDataString.append("					</use-template>");
        mockDataString.append("			</param>");
        mockDataString.append("      </response>");
        mockDataString.append("    </test>");
        mockDataString.append("  </category>");
        mockDataString.append("</mock-data>");
        mockData = unmarshallTestData(mockDataString.toString());
        manager.loadTestDataFromFile("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        catParam = new Param();
        catParam.setName("cat_param1");
        catParam.setValue("foo");
        manager.getTestData().getCategory(0).addParam(catParam);
        manager.addTestData(mockData);
        // original category had no name, new category had no name,
        // original category had parameters, new category had parameters
        // match, don't add category
        // new test has one request and one response with nested use-templates
        // and corresponding
        // child-parameters
        assertEquals("Only have 2 categories", 2, manager.getTestData().getCategoryCount());
        assertEquals("Has 0 templates", 0, manager.getTestData().getTemplateCount());
        assertEquals("cat_param1", manager.getTestData().getCategory(0).getParam(0).getName());
        assertEquals("Has 2 parameters in response", 2, manager.getTestData().getCategory(0).getTest(0).getResponse()
                .getParamCount());
        assertEquals("Has 1 use-template in response.param.child-params", 1, manager.getTestData().getCategory(0)
                .getTest(6).getResponse().getParam(0).getUseTemplateCount());
        assertEquals("ComplexClass-template", manager.getTestData().getCategory(0).getTest(6).getResponse().getParam(0)
                .getUseTemplate(0).getName());
        assertEquals("Has 2 child-parameters in response.param.child-params", 2, manager.getTestData().getCategory(0)
                .getTest(6).getResponse().getParam(0).getUseTemplate(0).getParamCount());
        assertEquals("booleanList", manager.getTestData().getCategory(0).getTest(6).getResponse().getParam(0)
                .getUseTemplate(0).getParam(0).getName());
        assertEquals("Has 3 parameters in response.param.child-params.param1.child-params", 3, manager.getTestData()
                .getCategory(0).getTest(6).getResponse().getParam(0).getUseTemplate(0).getParam(0)
                .getParamCount());
        assertEquals("child-template", manager.getTestData().getCategory(0).getTest(6).getResponse().getParam(0)
                .getUseTemplate(0).getParam(1).getUseTemplate(0).getName());
    }

    @Test()
    public void testAddTestTemplatedData() throws Exception {
        MockDataManager manager = new MockDataManager(
                "org/mdf/mockdata/MockDataManagerTemplateTestConfig.xml");
        // one category
        // one param in response with 35 child params
        assertEquals("Only have 1 category", 1, manager.getTestData().getCategoryCount());
        assertEquals("Has 0 templates", 0, manager.getTestData().getTemplateCount());
        assertEquals("Has 1 parameter in response", 1, manager.getTestData().getCategory(0).getTest(0).getResponse()
                .getParamCount());
        assertEquals("Has 35 parameters in response.param.child-params", 35, manager.getTestData().getCategory(0)
                .getTest(0).getResponse().getParam(0).getParamCount());
        assertEquals("E-Bookers Hotel", manager.getTestData().getCategory(0).getTest(0).getResponse().getParam(0)
                .getParam(2).getValue());
        assertEquals("Orbitz Hotel", manager.getTestData().getCategory(0).getTest(0).getResponse().getParam(0)
                .getParam(14).getValue());
        assertEquals("8", manager.getTestData().getCategory(0).getTest(0).getResponse().getParam(0)
                .getParam(23).getValue());

        // one category
        // one param in response with 23 child params
        // verify contents of selected tests
        assertEquals("Only have 1 category", 1, manager.getTestData().getCategoryCount());
        assertEquals("Has 0 templates", 0, manager.getTestData().getTemplateCount());
        assertEquals("Has 1 parameter in request", 1, manager.getTestData().getCategory(0).getTest(1).getRequest()
                .getParamCount());
        assertEquals("Has 23 parameters in request.param.child-params", 23, manager.getTestData().getCategory(0)
                .getTest(1).getRequest().getParam(0).getParamCount());
        assertEquals("10", manager.getTestData().getCategory(0).getTest(1).getRequest().getParam(0)
                .getParam(12).getValue());
        assertEquals("Milwaukee", manager.getTestData().getCategory(0).getTest(1).getRequest().getParam(0)
                .getParam(18).getValue());
        assertEquals("Has 3 total params", 3, manager.getTestData().getCategory(0).getTest(2).getRequest().getParam(0)
                .getParam(18).getParamCount());
        assertEquals("Munster", manager.getTestData().getCategory(0).getTest(2).getRequest().getParam(0)
                .getParam(18).getParam(0).getValue());
        assertEquals("IL", manager.getTestData().getCategory(0).getTest(2).getRequest().getParam(0)
                .getParam(18).getParam(1).getValue());
        assertEquals("exist", manager.getTestData().getCategory(0).getTest(2).getRequest().getParam(0)
                .getParam(18).getParam(2).getName());
        // nested templates with multiple templates at each level and multiple
        // parameters to be replaced within each use-template
        assertEquals("Has 5 total params", 5, manager.getTestData().getCategory(0).getTest(3).getRequest().getParam(0)
                .getParam(18).getParamCount());
        assertEquals("Munster", manager.getTestData().getCategory(0).getTest(3).getRequest().getParam(0)
                .getParam(18).getParam(0).getValue());
        assertEquals("IL", manager.getTestData().getCategory(0).getTest(3).getRequest().getParam(0)
                .getParam(18).getParam(1).getValue());
        assertEquals("exist", manager.getTestData().getCategory(0).getTest(3).getRequest().getParam(0)
                .getParam(18).getParam(2).getName());
        assertEquals("IL", manager.getTestData().getCategory(0).getTest(3).getRequest().getParam(0)
                .getParam(18).getParam(3).getValue());
        assertEquals("USA", manager.getTestData().getCategory(0).getTest(3).getRequest().getParam(0)
                .getParam(18).getParam(4).getValue());
        assertEquals("exist", manager.getTestData().getCategory(0).getTest(3).getRequest().getParam(0)
                .getParam(18).getParam(4).getParam(0).getName());
        // nested templates with multiple templates at each level and multiple
        // parameters to be replaced within each use-template. Added complexity
        // of
        // repeated use-templates, parameters defined out of order and repeated
        // parameters
        assertEquals("EBUK", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(11).getValue());
        assertEquals("OK", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(11).getParam(9)
                .getValue());
        assertEquals("100 Adams Street", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(11)
                .getParam(7).getValue());
        assertEquals("Suite 18000", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(11)
                .getParam(8).getValue());
        assertEquals("7", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(11).getParam(0)
                .getValue());
        assertEquals("EBUK Hotel", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(2).getValue());
        assertEquals("UK", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(2).getParam(1)
                .getValue());
        assertEquals("Ireland", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(10).getValue());
        assertEquals("IR", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(10).getParam(1)
                .getValue());
        assertEquals("TD", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(1).getValue());
        assertEquals("ND", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(1).getParam(9)
                .getValue());
        assertEquals("Gary", manager.getTestData().getCategory(0).getTest(4).getResponse().getParam(0)
                .getParam(18).getParam(4).getParam(18).getParam(0)
                .getValue());

        manager.loadTestDataFromFile("org/mdf/mockdata/MockDataManagerExclRulesTemplateTestConfig.xml");
        // nested child-parameters using similar hierarchy as
        // ExclusionRuleTestData
        assertEquals("Has 8 child-parameters in response.param.child-params", 8, manager.getTestData().getCategory(0)
                .getTest(0).getResponse().getParam(0).getParamCount());
        assertEquals("Has 3 child-parameters in response.param.child-params.use-template.param[5].child-params.param",
                3, manager.getTestData().getCategory(0).getTest(0).getResponse().getParam(0).getParam(
                        5).getParamCount());
        assertEquals("hotel:condition", manager.getTestData().getCategory(0).getTest(0).getResponse().getParam(0)
                .getParam(5).getParam(0).getName());
        assertEquals("hotel:trial3", manager.getTestData().getCategory(0).getTest(0).getResponse().getParam(0)
                .getParam(5).getParam(1).getValue());
        assertEquals("hotel:trial4", manager.getTestData().getCategory(0).getTest(0).getResponse().getParam(0)
                .getParam(5).getParam(2).getValue());
    }

    @Test()
    public void testMarshallTestDataValidation() throws Exception {
        MockData mockData = new MockData();
        mockData.addCategory(new Category());
        File f = new File("tmp");
        f.createNewFile();
        f.deleteOnExit();
        Exception e = null;
        try {
            MockDataManager.marshallTestData(mockData, "tmp");
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        assertTrue(e instanceof ValidationException);
        f.delete();
        f.createNewFile();
        MockDataManager.marshallTestData(mockData, "tmp", false);
    }

    @Test()
    public void testVariables() throws Exception {
        MockDataManager manager = new MockDataManager(
                "org/mdf/mockdata/MockDataManagerVariableTestConfig.xml");
        // category 0
        // --test 0
        assertEquals("var1", manager.getTestData().getCategory(0).getTest(0).getRequest().getParam(0).getName());
        assertEquals("cat_var1", manager.getTestData().getCategory(0).getTest(0).getResponse().getParam(0).getValue());
        // --test 1
        assertEquals("var1", manager.getTestData().getCategory(0).getTest(1).getRequest().getParam(0)
                .getParam(0).getValue());
        assertEquals("cat_var1", manager.getTestData().getCategory(0).getTest(1).getResponse().getParam(0)
                .getParam(0).getName());

        // category 1 - overridden global variable
        // --test 0
        assertEquals("cat2_var", manager.getTestData().getCategory(1).getTest(0).getRequest().getParam(0).getName());
        // --test 1
        assertEquals("cat2_var", manager.getTestData().getCategory(1).getTest(1).getResponse().getParam(0)
                .getParam(0).getValue());

        // category 2
        assertEquals("overrideable", manager.getTestData().getCategory(2).getTest(0).getRequest().getParam(0)
                .getValue());

        // category 3
        // --test 0
        assertEquals("templated_var", manager.getTestData().getCategory(3).getTest(0).getRequest().getParam(0)
                .getValue());
        // --test 1
        assertEquals("cat_var", manager.getTestData().getCategory(3).getTest(1).getRequest().getParam(0).getValue());
        assertEquals("templated_var", manager.getTestData().getCategory(3).getTest(1).getResponse().getParam(1)
                .getParam(0).getValue());
        
        // category 4
        // --test 4
        assertEquals("REPLACED.Extra", manager.getTestData().getCategory(4).getTest(0).getRequest().getParam(0)
                .getName());
        assertEquals("Prefixed.REPLACED.Value", manager.getTestData().getCategory(4).getTest(0).getRequest().getParam(1)
                .getValue());
        assertEquals("No Variable, No Value", manager.getTestData().getCategory(4).getTest(0).getRequest().getParam(2)
                .getName());
        assertNull( manager.getTestData().getCategory(4).getTest(0).getRequest().getParam(2)
                .getValue());
        assertEquals("PrefixedOnly.REPLACED", manager.getTestData().getCategory(4).getTest(0).getRequest().getParam(3)
                .getName());
        
        // ATEAM-459
        assertEquals("test@orbitz.com", manager.getTestData().getCategory(5).getTest(0).getRequest().getParam(0)
                .getValue());
        assertEquals("value_with@symbol", manager.getTestData().getCategory(5).getTest(0).getRequest().getParam(1)
                .getName());
    }

    @Test()
    public void testGregorianCalendar() throws Exception {
        MockDataManager manager = new MockDataManager();
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        Calendar gc = GregorianCalendar.getInstance();
        ReflectionParamBuilderUtil3.buildParamFromObject(gc, objectParam);
        MockData data = new MockData();
        data.addCategory(new Category());
        data.getCategory(0).addTest(new org.mdf.mockdata.generated.Test());
        data.getCategory(0).getTest(0).setRequest(new Request());
        data.getCategory(0).getTest(0).setResponse(new Response());
        data.getCategory(0).getTest(0).getResponse().addParam(objectParam);
        manager.addTestData(data);
        Param[] response = manager.findResponse(new ArrayList<Param>());
        assertNotNull(response);
        GregorianCalendar rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(response[0],
                GregorianCalendar.class);
        assertEquals(gc, rehydrated);
    }

    @Test()
    public void testDebugOutputWithoutXmlHeader() throws Exception {
        Pattern xmlDeclarationPattern = Pattern.compile("[\\s\\n\\r]*<\\?xml\\s+version(.*\\s)?encoding(.*)?\\?>");

        MockData data = new MockData();
        Category category = new Category();
        category.setName("category");
        data.addCategory(category);
        org.mdf.mockdata.generated.Test test = new org.mdf.mockdata.generated.Test();
        category.addTest(test);
        Request request = new Request();
        test.setRequest(request);
        Param reqParam = new Param();
        reqParam.setName("name");
        reqParam.setValue("value");
        request.addParam(reqParam);

        Response response = new Response();
        test.setResponse(response);
        Param respParam = new Param();
        respParam.setName("resp name");
        respParam.setValue("resp value");
        response.addParam(respParam);
        XMLContext context = new XMLContext();
        context.setProperty("org.exolab.castor.indent", true);

        StringWriter writer = new StringWriter();
        Marshaller marshaller = new Marshaller(writer);
        marshaller.setWriter(writer);
        marshaller.marshal(data);
        String temp = "";
        if (writer.toString().contains("<?xml version")) {
            Matcher m = xmlDeclarationPattern.matcher(writer.toString());
            temp = m.replaceFirst("");
        }
        assertFalse(temp.contains("xml version"));

    }

    @Test()
    public void testCreateException() throws Exception {
        MockDataManager manager = new MockDataManager();
        Param exceptionParam = new Param();
        exceptionParam.setName("exceptionParam");
        Param childParam = new Param();
        childParam.setName("detailMessage");
        childParam.setValue("An exception was thrown");
        exceptionParam.addParam(childParam);
        MockData data = new MockData();
        data.addCategory(new Category());
        data.getCategory(0).addTest(new org.mdf.mockdata.generated.Test());
        data.getCategory(0).getTest(0).setRequest(new Request());
        data.getCategory(0).getTest(0).setResponse(new Response());
        data.getCategory(0).getTest(0).getResponse().addParam(exceptionParam);
        manager.addTestData(data);
        Param[] response = manager.findResponse(new ArrayList<Param>());
        assertNotNull(response);
        Exception e = ReflectionParamBuilderUtil3.buildObjectFromParams(response[0], Exception.class);
        assertEquals("An exception was thrown", e.getMessage());
    }

    @Test()
    public void testDelay() throws Exception {
        MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        Param requestParam = new Param();
        requestParam.setName("delay_test");
        requestParam.setValue("true");
        long start = System.currentTimeMillis();
        Param[] response = manager.findResponse(Arrays.asList(new Param[] { requestParam }));
      //Thread.sleep(millis) is not accurate to the millisecond, so i pad the timer here a bit to make it go
        Thread.sleep(10);
        long elapsedTime = System.currentTimeMillis() - start;
        assertNotNull(response);
        assertTrue(elapsedTime >= Long.parseLong(response[0].getValue()));
    }
    
    @Test()
    public void testTwoDeepTemplate() throws Exception {
    	MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockAdvancedTemplateConfig.xml");
    	assertEquals("foo", manager.getTestData().getCategory(0).getTest(0).getResponse().
    			getParam(0).getParam(2).getParam(0).
    			getParam(0).getParam(1).getName());
    	
    }
    
    @Test()
    public void testNestedReplacement() throws Exception {
    	MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockAdvancedTemplateConfig.xml");
    	assertEquals(3,manager.getTestData().getCategory(0).getTest(1).getResponse().getParam(0)
    			.getParamCount());
    	assertEquals("first", manager.getTestData().getCategory(0).getTest(1).getResponse().
    			getParam(0).getParam(0).getName());
    	assertEquals("level1.first replaced", manager.getTestData().getCategory(0).getTest(1).getResponse().
    			getParam(0).getParam(0).getValue());
    	
    	assertEquals("first.second", manager.getTestData().getCategory(0).getTest(1).getResponse().
    			getParam(0).getParam(1).getName());
    	assertEquals("level1.first.second replaced", manager.getTestData().getCategory(0).getTest(1).getResponse().
    			getParam(0).getParam(1).getValue());
    	
    	assertEquals("third", manager.getTestData().getCategory(0).getTest(1).getResponse().
    			getParam(0).getParam(1).getParam(0).getName());
    	assertEquals("third replaced", manager.getTestData().getCategory(0).getTest(1).getResponse().
    			getParam(0).getParam(1).getParam(0).getValue());
    	
    }
    
    @Test()
    public void testNestedReplacementWithBrackets() throws Exception {
    	MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockAdvancedTemplateConfig.xml");
    	assertEquals(3,manager.getTestData().getCategory(0).getTest(2).getResponse().getParam(0)
    			.getParamCount());
    	assertEquals("first", manager.getTestData().getCategory(0).getTest(2).getResponse().
    			getParam(0).getParam(0).getName());
    	assertEquals("level1.first replaced", manager.getTestData().getCategory(0).getTest(2).getResponse().
    			getParam(0).getParam(0).getValue());
    	
    	assertEquals("first.second", manager.getTestData().getCategory(0).getTest(2).getResponse().
    			getParam(0).getParam(1).getName());
    	assertEquals("level1.first.second replaced", manager.getTestData().getCategory(0).getTest(2).getResponse().
    			getParam(0).getParam(1).getValue());
    	
    	assertEquals("fourth", manager.getTestData().getCategory(0).getTest(2).getResponse().
    			getParam(0).getParam(2).getParam(0).getName());
    	assertEquals("fourth replaced", manager.getTestData().getCategory(0).getTest(2).getResponse().
    			getParam(0).getParam(2).getParam(0).getValue());
    	
    }
    
    @Test()
    public void testSideBySide() throws Exception {
    	MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockAdvancedTemplateConfig.xml");
    	assertEquals(2,manager.getTestData().getCategory(0).getTest(3).getResponse().getParamCount());
    	assertEquals("fourth",manager.getTestData().getCategory(0).getTest(3).getResponse().getParam(0).getName());
    	assertEquals("exist",manager.getTestData().getCategory(0).getTest(3).getResponse().getParam(1).getName());
    }
    
    @Test
    public void nestedMorphers() throws Exception {
        MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockDataManagerTestConfig.xml");
        Param requestParam = new Param();
        requestParam.setName("nested_morphers");
        requestParam.setValue("true");
        Param[] response = manager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(2, response.length);
        assertEquals(new DateMidnight().plusDays(1).toString("yyyy-MM-dd"), response[0].getValue());
        assertEquals(new DateMidnight().plusDays(2).toString("yyyy-MM-dd"), response[1].getValue());
    }
    @Test
    public void testInheritedTemplates() throws Exception {
    	MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockAdvancedTemplateConfig.xml");
    	assertEquals(8,manager.getTestData().getCategory(0).getTest(5).getResponse().getParamCount());
    	assertEquals("old_param",manager.getTestData().getCategory(0).getTest(5).getResponse().getParam(0).getName());
    	assertEquals("top_param",manager.getTestData().getCategory(0).getTest(5).getResponse().getParam(1).getName());
    	assertEquals("inherited_param",manager.getTestData().getCategory(0).getTest(5).getResponse().getParam(2).getName());
    	assertEquals("existing",manager.getTestData().getCategory(0).getTest(5).getResponse().getParam(3).getName());
    	assertEquals("existing",manager.getTestData().getCategory(0).getTest(5).getResponse().getParam(4).getName());
    	assertEquals("new",manager.getTestData().getCategory(0).getTest(5).getResponse().getParam(5).getName());
    	assertEquals("existing",manager.getTestData().getCategory(0).getTest(5).getResponse().getParam(3).getName());
    	assertEquals("existing",manager.getTestData().getCategory(0).getTest(5).getResponse().getParam(4).getName());

    }
    
    @Test
    public void testComplexInheritedTemplates() throws Exception {
        MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockAdvancedTemplateConfig.xml");
        Param requestParam = new Param();
        requestParam.setName("param5");
        requestParam.setValue("value");
        Param[] response = manager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(1, response.length);
        assertEquals("list",response[0].getName());
        assertEquals("object",response[0].getParam(0).getName());
        assertEquals("field", response[0].getParam(0).getParam(0).getName());
        assertEquals("common_field", response[0].getParam(0).getParam(1).getName());
        assertEquals(2, response[0].getParam(0).getParam(1).getParamCount());
        assertEquals("common-a", response[0].getParam(0).getParam(1).getParam(0).getName());
        assertEquals("a", response[0].getParam(0).getParam(1).getParam(0).getParam(0).getName());
        assertEquals("1", response[0].getParam(0).getParam(1).getParam(0).getParam(0).getValue());
        assertEquals(1, response[0].getParam(0).getParam(1).getParam(0).getParamCount());

        assertEquals("object",response[0].getParam(1).getName());
        assertEquals("field", response[0].getParam(1).getParam(0).getName());
        assertEquals("this is another field", response[0].getParam(1).getParam(0).getValue());
        assertEquals("common_field", response[0].getParam(1).getParam(1).getName());
        assertEquals("common-a", response[0].getParam(1).getParam(1).getParam(0).getName());
        assertEquals("a", response[0].getParam(1).getParam(1).getParam(0).getParam(0).getName());
        assertEquals("2", response[0].getParam(1).getParam(1).getParam(0).getParam(0).getValue());
        assertEquals(1, response[0].getParam(1).getParam(1).getParam(0).getParamCount());

        assertEquals("object",response[0].getParam(2).getName());
        assertEquals("field", response[0].getParam(2).getParam(0).getName());
        assertEquals("common_field", response[0].getParam(2).getParam(1).getName());
        assertEquals("common-a", response[0].getParam(2).getParam(1).getParam(0).getName());
        assertEquals("a", response[0].getParam(2).getParam(1).getParam(0).getParam(0).getName());
        assertEquals("3", response[0].getParam(2).getParam(1).getParam(0).getParam(0).getValue());
        assertEquals(1, response[0].getParam(2).getParam(1).getParam(0).getParamCount());

        assertEquals("object",response[0].getParam(3).getName());
        assertEquals("field", response[0].getParam(3).getParam(0).getName());
        assertEquals("common_field", response[0].getParam(3).getParam(1).getName());
        assertEquals("common-b", response[0].getParam(3).getParam(1).getParam(1).getName());
        assertEquals("foo", response[0].getParam(3).getParam(1).getParam(1).getValue());
        assertEquals(1, response[0].getParam(3).getParam(1).getParam(0).getParamCount());
    }
    
    @Test
    public void testComplexObjectTemplate() throws Exception {
        ComplexClass original = new ComplexClass(new HashMap<String, ComplexClass2>(), true);
        MockDataManager manager = new MockDataManager("org/mdf/mockdata/MockDataManagerComplexNestedTemplateData.xml");
        Param requestParam = new Param();
        requestParam.setName("p");
        requestParam.setValue("test_1");
        Param[] response = manager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertNotNull(response);
        assertEquals(1, response.length);
        ComplexClass rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(response[0], ComplexClass.class);
        assertEquals(original, rehydrated);
        assertEquals(original.hashCode(), rehydrated.hashCode());

        requestParam.setValue("test_2");
        response = manager.findResponse(Arrays.asList(new Param[] { requestParam }));
        assertEquals(1, response.length);
        rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(response[0], ComplexClass.class);
        assertFalse(rehydrated.getChild().getBooleanList().get(0));
    }
    
    private MockData unmarshallTestData(String mockDataString) throws MarshalException, ValidationException {
        XMLContext context = new XMLContext();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setClass(MockData.class);
        MockData mockData = (MockData) unmarshaller.unmarshal(new StringReader(mockDataString));
        return mockData;
    }

}
