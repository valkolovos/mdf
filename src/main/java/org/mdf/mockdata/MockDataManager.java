package org.mdf.mockdata;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.CategoryMatcher;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Test;

import com.sun.xml.internal.ws.wsdl.writer.document.ParamType;
import com.thoughtworks.xstream.InitializationException;

/**
 * This is the main class for the mock data framework. Typical usage would be to
 * construct a MockDataManager with no data and feed data from the client using
 * the MBeanMockDataManager library. Custom category and parameter matchers can
 * be injected in mock data file. The primary method
 * used at runtime is <a href="#findResponse(com.orbitz.servicetests.mockdata.generated.CategoryParams,%20com.orbitz.servicetests.mockdata.generated.RequestParams)"
 * >findResponse</a>.
 */
public class MockDataManager {

    public static final String CAPTURED_PARAM_NAME = "captured_name";

    /**
     * The test data to operate with
     */
    private MockData _testData = new MockData();

    /**
     * The {@link ParameterMatcher} for category matching
     */
    private ParameterMatcher _categoryMatcher;

    /**
     * The {@link ParameterMatcher} for test parameter matching
     */
    private ParameterMatcher _parameterMatcher;

    /**
     * List of {@link ParameterMorpher} objects to use
     */
    private List<ParameterMorpher> _parameterMorpherList = new ArrayList<ParameterMorpher>();

    /**
     * Maintains a mapping of all parameter matchers by id
     */
    private Map<String, ParameterMatcher> _parameterMatcherMap = new HashMap<String, ParameterMatcher>();

    /**
     * Maintains a mapping of all parameter morphers by id
     */
    private Map<String, ParameterMorpher> _parameterMorpherMap = new HashMap<String, ParameterMorpher>();

    private MockData _capturedData = new MockData();
    
    private boolean _useCache = false;
    
    private Map<Integer, MatchResult> _cache = new HashMap<Integer, MatchResult>();

    /**
     * Default constructor
     */
    public MockDataManager() throws InstantiationException {
        this((String) null);
    }

    /**
     * Construct a MockDataManager with given {@link TestData}
     * 
     * @param testData
     *            the test data to use
     */
    public MockDataManager(MockData testData) {
        _testData = testData;
        initializeMatcherMorpherConfig(testData);
    }

    /**
     * Constructs a MockDataManager and loads the test data from the given file.
     * This constructor will check the file as an absolute file name as well as
     * within the current classpath.
     * 
     * @param dataFile
     *            The name of the datafile to load
     * @throws InstantiationException
     *             if the data file cannot be loaded
     */
    public MockDataManager(String dataFile) throws InstantiationException {
        _capturedData.addCategory(new Category());
        if (dataFile != null && !"".equals(dataFile)) {
            try {
                loadTestDataFromFile(dataFile);
            } catch (Exception e) {
                Logger.getLogger(getClass()).error("Unable to instantiate MockDataManager", e);
                e.printStackTrace();
                throw new InstantiationException("Unable to instantiate MockDataManager");
            }
        }
    }

    /**
     * Attempts to load the TestData object from the given file. This method
     * will check the file as an absolute file name as well as check the current
     * classpath.
     * 
     * @param dataFile
     *            The name of the data file to load
     * @throws Exception
     *             If something goes wrong with parsing the data file
     */
    public void loadTestDataFromFile(String dataFile) throws Exception {
        MockDataLoader mockDataLoader = new MockDataLoader();
        MockData mockData = mockDataLoader.loadMockData(dataFile);
        mockDataLoader.readTestDataFromFile(mockData);
        _testData = mockData;
        initializeMatcherMorpherConfig(mockData);
    }
    
    /**
     * Sets test data to given mock data and resets configuration
     * from given mock data.
     * @param data Mock Data to use
     * @since 3.0
     */
    public void setMockData(MockData data) throws Exception {
        MockDataLoader mockDataLoader = new MockDataLoader();
        mockDataLoader.readTestDataFromFile(data);
        _testData = data;
        initializeMatcherMorpherConfig(data);
    }

    /**
     * Sets test data to given mock data and resets configuration
     * from given mock data.
     * @param data Mock Data to use
     * @since 3.0
     */
    public void setMockData(MockData data, Map<String, String> contextVariables) throws Exception {
        MockDataLoader mockDataLoader = new MockDataLoader();
        mockDataLoader.setGlobalVariableMap(contextVariables);
        mockDataLoader.readTestDataFromFile(data);
        _testData = data;
        initializeMatcherMorpherConfig(data);
    }

    /**
     * @param categoryMatcher
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public void setCategoryMatcher(ParameterMatcher categoryMatcher) {
        _parameterMatcherMap.put(generateMatcherId(categoryMatcher), categoryMatcher);
        _categoryMatcher = categoryMatcher;
    }

    public ParameterMatcher getCategoryMatcher() {
        return _categoryMatcher;
    }

    /**
     * @param parameterMatcher
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public void setParameterMatcher(ParameterMatcher parameterMatcher) {
        _parameterMatcher = parameterMatcher;
    }

    public ParameterMatcher getParameterMatcher() {
        return _parameterMatcher;
    }

    /**
     * @param parameterMorpherList
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public void setParameterMorpherList(List<ParameterMorpher> parameterMorpherList) {
        _parameterMorpherList = parameterMorpherList;
        for (ParameterMorpher morpher : parameterMorpherList) {
            _parameterMorpherMap.put(generateMorpherId(morpher), morpher);
        }
    }

    /**
     * @param parameterMorpherList
     * @deprecated - Initialize using mock data file and initialization
     *             parameters instead
     */
    public void setTestRequestMorpherList(List<ParameterMorpher> parameterMorpherList) {
        _parameterMorpherList.addAll(parameterMorpherList);
        for (ParameterMorpher morpher : parameterMorpherList) {
            _parameterMorpherMap.put(generateMorpherId(morpher), morpher);
        }
    }

    public ParameterMorpher getParameterMorpher(String id) {
        return _parameterMorpherMap.get(id);
    }

    public Param[] findResponse(List<Param> params) throws Exception {
        return findResponse(null, params);
    }

    /**
     * This is the primary runtime method of the MockDataManager. This method
     * takes the given category and request parameters and attempts to find the
     * appropriate data. This method will use the MockDataManager's category and
     * parameter matchers if they exist. If parameters are found, the
     * MockDataManager's parameter morphers (if specified) will be invoked on
     * the appropriate parameters
     * 
     * @param categoryParams
     *            The category parameters to match on. These will be ignored for
     *            Tests that have no category parameters.
     * @param params
     *            The request parameters to match on.
     * @return an array of {@link ParamType} objects found in the TestData
     * @throws Exception
     */
    public Param[] findResponse(List<Param> categoryParams, List<Param> requestParams) throws Exception {
        long startTime = System.currentTimeMillis();
        logRequestParams(categoryParams, requestParams);
        int cacheKey = -1;
        if (_useCache) {
            HashCodeBuilder hcb = new HashCodeBuilder();
            for (Param p : categoryParams) {
                populateHashBuilder(hcb, p);
            }
            for (Param p : requestParams) {
                populateHashBuilder(hcb, p);
            }
            cacheKey = hcb.toHashCode();
            MatchResult matchResult = _cache.get(cacheKey);
            if (matchResult != null) {
                if (matchResult.test.get(0).getResponse().getParam() != null) {
                    if (matchResult.test.get(0).getResponse().hasDelay()) {
                        long delay = matchResult.test.get(0).getResponse().getDelay();
                        if (System.currentTimeMillis() < (startTime + delay)) {
                            Thread.sleep((startTime + delay) - System.currentTimeMillis());
                        }
                    }
                }
                return matchResult.finalParams.toArray(new Param[matchResult.finalParams.size()]);
            }
        }
        if (_categoryMatcher == null) {
            _categoryMatcher = new DefaultParameterMatcher();
        }
        if (_parameterMatcher == null) {
            _parameterMatcher = new DefaultParameterMatcher();
        }
        MatchResult matchResult = match(categoryParams, requestParams, _testData, _categoryMatcher, _parameterMatcher,
                _parameterMorpherList);
        long delay = 0;
        List<Param> rowList = new ArrayList<Param>();
        if (matchResult != null) {
            if (matchResult.test.get(0).getResponse().getParam() != null) {
                if (matchResult.test.get(0).getResponse().hasDelay()) {
                    delay = matchResult.test.get(0).getResponse().getDelay();
                }
                for (int i = 0; i <  matchResult.test.get(0).getResponse().getParamCount(); i++) {
                    Param responseParam = matchResult.test.get(0).getResponse().getParam(i);
                    rowList.addAll(buildResults(matchResult.categoryName, responseParam));
                }
            }
        }
        if (System.currentTimeMillis() < (startTime + delay)) {
            Thread.sleep((startTime + delay) - System.currentTimeMillis());
        }
        if (_useCache) {
            if (matchResult != null) {
                matchResult.finalParams = rowList;
                _cache.put(cacheKey, matchResult);
            }
        }
        return rowList.toArray(new Param[rowList.size()]);
    }
    
    private void populateHashBuilder(HashCodeBuilder hcb, Param p) {
        hcb.append(p.getName()).append(p.getValue());
        for (int i = 0; i < p.getParamCount(); i++) {
            populateHashBuilder(hcb, p.getParam(i));
        }
    }
    
    /**
     * Adds data to existing test data. This will look through the passed-in
     * test data and try to match categories by name and then by parameters. If
     * no matching category is found, a new one will be created.
     * 
     * @param testData
     */
    public void addTestData(MockData testData) {
        DefaultParameterMatcher p = new DefaultParameterMatcher();
        for (Category category : testData.getCategory()) {
            Category catToAddTestsTo = null;
            for (Category existingCategory : _testData.getCategory()) {
                if (category.getName() != null && existingCategory.getName() == null) {
                    continue;
                }
                if (category.getName() == null && existingCategory.getName() != null) {
                    continue;
                }
                if (category.getName() == null && existingCategory.getName() == null) {
                    // match both ways
                    if (p.paramsMatch(Arrays.asList(existingCategory.getParam()), category.getParam())
                            && p.paramsMatch(Arrays.asList(category.getParam()), existingCategory.getParam())) {
                        catToAddTestsTo = existingCategory;
                    }
                    break;
                }
                if (existingCategory.getName().equals(category.getName())) {
                    catToAddTestsTo = existingCategory;
                    break;
                }
            }
            if (catToAddTestsTo != null) {
                for (Test t : category.getTest()) {
                    catToAddTestsTo.addTest(t);
                }
            } else {
                _testData.addCategory(category);
            }
        }
    }

    public boolean verifyCapturedData(List<Param> params) throws Exception {
        return verifyCapturedData(null, params);
    }

    public boolean verifyCapturedData(List<Param> categoryParams, List<Param> requestParams) throws Exception {
        DefaultParameterMatcher matcher = new DefaultParameterMatcher();
        Param initParam = new Param();
        initParam.setName("reverse");
        initParam.setValue("true");
        matcher.setInitParams(initParam);
        MatchResult result = match(categoryParams, requestParams, _capturedData, matcher,
                matcher, new ArrayList<ParameterMorpher>());
        return result != null && result.test.size() > 0;
    }

    public List<Test> findCapturedData(List<Param> requestParams) throws Exception {
        return findCapturedData(null, requestParams);
    }

    public List<Test> findCapturedData(List<Param> categoryParams, List<Param> requestParams) throws Exception {
        DefaultParameterMatcher matcher = new DefaultParameterMatcher();
        Param initParam = new Param();
        initParam.setName("reverse");
        initParam.setValue("true");
        matcher.setInitParams(initParam);
        MatchResult result = match(categoryParams, requestParams, _capturedData, matcher,
                matcher, new ArrayList<ParameterMorpher>(), true);
        if (result != null) {
            return result.test;
        } else {
            return new ArrayList<Test>();
        }
    }

    public void resetCapturedData() {
        _capturedData = new MockData();
        _capturedData.addCategory(new Category());
    }

    public static void marshallTestData(MockData mockData, String filename) throws IOException, MarshalException,
            ValidationException {
        marshallTestData(mockData, filename, true);
    }

    /**
     * Convenience method to write out the MockData xml file
     * 
     * @param mockData
     *            the {@link MockData} to write out
     * @param filename
     *            The name of the file to write to. This should be specified as
     *            an absolute path name.
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    public static void marshallTestData(MockData mockData, String filename, boolean validate) throws IOException,
            MarshalException, ValidationException {
        XMLContext xmlContext = new XMLContext();
        xmlContext.setProperty("org.exolab.castor.indent", true);
        Marshaller m = xmlContext.createMarshaller();
        m.setValidation(validate);
        FileWriter w = new FileWriter(filename);
        m.setWriter(w);
        m.marshal(mockData);
    }
    
    /**
     * Turns on cache for Mock Data Manager. When caching is on, parameter morphers and matchers are only
     * applied when a request is not found in the cache.
     * @param useCache
     * @since 3.12
     */
    public void setUseCache(boolean useCache) {
        _useCache = useCache;
    }
    
    public boolean isUseCache() {
        return _useCache;
    }
    
    public void clearCache() {
        _cache.clear();
    }

    protected MatchResult match(List<Param> categoryParams, List<Param> requestParams, MockData testData,
            ParameterMatcher categoryMatcher, ParameterMatcher parameterMatcher,
            List<ParameterMorpher> parameterMorpherList) throws Exception {
        return match(categoryParams, requestParams, testData, categoryMatcher, parameterMatcher, parameterMorpherList, false);
    }
    protected MatchResult match(List<Param> categoryParams, List<Param> requestParams, MockData testData,
            ParameterMatcher categoryMatcher, ParameterMatcher parameterMatcher,
            List<ParameterMorpher> parameterMorpherList, boolean findAll) throws Exception {
        Logger logger = Logger.getLogger(getClass());
        MatchResult result = null;
        for (Category category : testData.getCategory()) {
            if (category.getParamCount() > 0) {
                if (categoryParams == null) {
                    continue;
                }
                if (!categoryMatcher.paramsMatch(categoryParams, category.getParam())) {
                    continue;
                }
            }
            List<Param> morphedParams = new ArrayList<Param>();
            for (Param requestParam : requestParams) {
                morphedParams.addAll(buildResults(category.getName(), requestParam));
            }
            for (Test test : category.getTest()) {
                List<Param> testRequestParams = new ArrayList<Param>();
                for (int i = 0; i < test.getRequest().getParamCount(); i++) {
                    Param requestParam = test.getRequest().getParam(i);
                    testRequestParams.addAll(buildResults(category.getName(), requestParam));
                }
                if (parameterMatcher.paramsMatch(morphedParams, testRequestParams.toArray(new Param[testRequestParams
                        .size()]))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Matched - Returned the following test");
                        try {
                            XMLContext xmlContext = new XMLContext();
                            xmlContext.setProperty("org.exolab.castor.indent", true);
                            Marshaller m = xmlContext.createMarshaller();
                            StringWriter w = new StringWriter();
                            m.setWriter(w);
                            m.marshal(test);
                            logger.debug(w.toString());
                        } catch (Exception e) {
                        }
                    }
                    if (result == null) {
                        result = new MatchResult(category.getName(), test);
                    } else {
                        result.test.add(test);
                    }
                    
                    if (!findAll) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    protected MockData getTestData() {
        return _testData;
    }

    protected void addCapturedData(Test test) {
        addCapturedData(null, test);
    }

    protected void addCapturedData(String categoryName, Test test) {
        Category catToAddTest = null;
        for (Category c : _capturedData.getCategory()) {
            if ((c.getName() == null && categoryName == null)
                    || (categoryName != null && categoryName.equals(c.getName()))) {
                catToAddTest = c;
                break;
            }
        }
        if (catToAddTest == null) {
            catToAddTest = new Category();
            catToAddTest.setName(categoryName);
            if (categoryName != null && !"".equals(categoryName)) {
                Param catParam = new Param();
                catParam.setName(CAPTURED_PARAM_NAME);
                catParam.setValue(categoryName);
                catToAddTest.addParam(catParam);
            }
            _capturedData.addCategory(catToAddTest);
        }
        catToAddTest.addTest(test);
    }

    void logRequestParams(List<Param> categoryParams, List<Param> requestParams) {
        Logger logger = Logger.getLogger(getClass());
        if (logger.isDebugEnabled()) {
            Param tmpParam = new Param();
            tmpParam.setName("DebugParam");
            Param catParams = new Param();
            catParams.setName("Category Parameters");
            tmpParam.addParam(catParams);
            if (categoryParams != null && categoryParams.size() > 0) {
                catParams.setParam(categoryParams.toArray(new Param[categoryParams.size()]));
            }
            Param reqParams = new Param();
            reqParams.setName("Request Parameters");
            tmpParam.addParam(reqParams);
            if (requestParams != null && requestParams.size() > 0) {
                reqParams.setParam(requestParams.toArray(new Param[requestParams.size()]));
            }
            try {
                XMLContext xmlContext = new XMLContext();
                xmlContext.setProperty("org.exolab.castor.indent", true);
                Marshaller m = xmlContext.createMarshaller();
                StringWriter w = new StringWriter();
                m.setWriter(w);
                m.marshal(tmpParam);
                logger.debug(w.toString());
            } catch (Exception e) {
            }
        }
    }
    
    private List<Param> buildResults(String categoryName, Param responseParam) throws Exception {
        List<Param> results = new ArrayList<Param>();
        boolean morphed = false;
        List<ParameterMorpher> _deferredMorphers = new ArrayList<ParameterMorpher>();
        for (ParameterMorpher paramMorpher : _parameterMorpherList) {
            if (paramMorpher.canMorphParameter(categoryName, responseParam.getName())) {
                if (paramMorpher instanceof DeferredParameterMorpher) {
                    _deferredMorphers.add(paramMorpher);
                    continue;
                }
                morphed = true;
                List<Param> morphedParams = paramMorpher.morphParameter(responseParam);
                for (Param morphedParam : morphedParams) {
                    if (paramMorpher instanceof ChainedParameterMorpher) {
                        results.addAll(buildResults(categoryName, morphedParam));
                    } else {
                        results.add(buildResponseParam(categoryName, morphedParam));
                    }
                }
            }
        }

        // default handling
        if (!morphed) {
            results.add(buildResponseParam(categoryName, responseParam));
        }
        List<Param> tmp = new ArrayList<Param>();
        for (Iterator<Param> iter = results.iterator(); iter.hasNext(); ) {
            Param p = iter.next();
            iter.remove();
            List<Param> morphedParams = null;
            for (ParameterMorpher paramMorpher : _deferredMorphers) {
                if (paramMorpher.canMorphParameter(categoryName, p.getName())) {
                    morphedParams = paramMorpher.morphParameter(p);
                    for (Param morphedParam : morphedParams) {
                        if (paramMorpher instanceof ChainedParameterMorpher) {
                            tmp.addAll(buildResults(categoryName, morphedParam));
                        } else {
                            tmp.add(buildResponseParam(categoryName, morphedParam));
                        }
                    }
                }
            }
            if (morphedParams == null) {
                tmp.add(p);
            }
        }
        
        return tmp;
    }

    private Param buildResponseParam(String categoryName, Param morphedParam) throws Exception {
        Param newResponseParam = new Param();
        newResponseParam.setName(morphedParam.getName());
        newResponseParam.setType(morphedParam.getType());
        newResponseParam.setValue(morphedParam.getValue());
        for (int i = 0; i < morphedParam.getParamCount(); i++) {
            Param childParam = morphedParam.getParam(i);
            for (Param morphedChildParam : buildResults(categoryName, childParam)) {
                newResponseParam.addParam(morphedChildParam);
            }
        }
        return newResponseParam;
    }

    private class MatchResult {
        String categoryName;
        List<Test> test;
        List<Param> finalParams;

        MatchResult(String categoryName, Test test) {
            this.categoryName = categoryName;
            this.test = new ArrayList<Test>();
            this.test.add(test);
        }
    }

    public boolean validateCapturedData(String table, String column, String value) throws Exception {
        Map<String, String> values = new HashMap<String, String>(1);
        values.put(column, value);
        return validateCapturedData(table, values);
    }

    public boolean validateCapturedData(String table, Map<String, String> values) throws Exception {
        Param catParam = new Param();
        catParam.setName(MockDataManager.CAPTURED_PARAM_NAME);
        catParam.setValue(table);

        List<Param> columnParams = new ArrayList<Param>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Param columnParam = new Param();
            columnParam.setName(entry.getKey());
            columnParam.setValue(entry.getValue());
            columnParams.add(columnParam);
        }

        return verifyCapturedData(Arrays.asList(new Param[] { catParam }), columnParams);
    }

    private String generateMatcherId(ParameterMatcher matcher) {
        int i = 1;
        String id = null;
        while (id == null) {
            String tmpId = matcher.getClass().getName() + Integer.toString(i);
            if (!_parameterMatcherMap.containsKey(tmpId)) {
                id = tmpId;
            } else {
                i++;
            }
        }
        return id;
    }

    private String generateMorpherId(ParameterMorpher morpher) {
        int i = 1;
        String id = null;
        while (id == null) {
            String tmpId = morpher.getClass().getName() + Integer.toString(i);
            if (!_parameterMorpherMap.containsKey(tmpId)) {
                id = tmpId;
            } else {
                i++;
            }
        }
        return id;
    }

    private void initializeMatcherMorpherConfig(MockData mockData) throws InitializationException {
        try {
            _categoryMatcher = initializeParameterMatcher(mockData.getCategoryMatcher());
            _parameterMatcher = initializeParameterMatcher(mockData.getParameterMatcher());
            _parameterMorpherList = initializeParameterMorpherList(mockData.getParameterMorpher());
        } catch (Exception e) {
            throw new InitializationException("Unable to initialize matchers and morphers", e);
        }
    }

    private ParameterMatcher initializeParameterMatcher(CategoryMatcher categoryMatcherParam)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (categoryMatcherParam == null) {
            return null;
        }
        ParameterMatcher categoryMatcher = initializeParameterMatcher(categoryMatcherParam.getClazz(),
                categoryMatcherParam.getId(), categoryMatcherParam.getParam());
        if (categoryMatcher instanceof DelegatingParameterMatcher) {
            ((DelegatingParameterMatcher) categoryMatcher)
                    .setDelegateParameterMatcher(initializeParameterMatcher(categoryMatcherParam.getCategoryMatcher()));
        }
        if (categoryMatcher instanceof MockDataAwareParameterMatcher) {
            ((MockDataAwareParameterMatcher) categoryMatcher).setMockDataManager(this);
        }
        return categoryMatcher;
    }

    private ParameterMatcher initializeParameterMatcher(
            org.mdf.mockdata.generated.ParameterMatcher paramMatcherParam)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (paramMatcherParam == null) {
            return null;
        }
        ParameterMatcher categoryMatcher = initializeParameterMatcher(paramMatcherParam.getClazz(), paramMatcherParam
                .getId(), paramMatcherParam.getParam());
        if (categoryMatcher instanceof DelegatingParameterMatcher) {
            ParameterMatcher delegateMatcher = initializeParameterMatcher(paramMatcherParam.getParameterMatcher());
            if (delegateMatcher != null) {
                ((DelegatingParameterMatcher) categoryMatcher).setDelegateParameterMatcher(delegateMatcher);
            }
        }
        if (categoryMatcher instanceof MockDataAwareParameterMatcher) {
            ((MockDataAwareParameterMatcher) categoryMatcher).setMockDataManager(this);
        }
        return categoryMatcher;
    }

    private ParameterMatcher initializeParameterMatcher(String clazz, String id, Param[] initParams)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ParameterMatcher matcher = (ParameterMatcher) Class.forName(clazz).newInstance();
        matcher.setInitParams(initParams);
        String mapId = id == null ? generateMatcherId(matcher) : id;
        ParameterMatcher previousMatcher = _parameterMatcherMap.put(id, matcher);
        if (previousMatcher != null) {
            Logger.getLogger(getClass()).warn(
                    "Duplicate parameter matcher id of " + mapId + "found. Replacing old matcher in map.");
        }
        return matcher;
    }

    private List<ParameterMorpher> initializeParameterMorpherList(
            org.mdf.mockdata.generated.ParameterMorpher[] morpherParams) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        List<ParameterMorpher> paramMorpherList = new ArrayList<ParameterMorpher>();
        for (org.mdf.mockdata.generated.ParameterMorpher morpherParam : morpherParams) {
            if (morpherParam.getRef() != null) {
                ParameterMorpher referencedMorpher = _parameterMorpherMap.get(morpherParam.getRef());
                if (referencedMorpher == null) {
                    // some matchers are morphers too
                    // this will obviously throw a ClassCastException if the
                    // reference
                    // is not a ParameterMorpher
                    try {
                        referencedMorpher = (ParameterMorpher) _parameterMatcherMap.get(morpherParam.getRef());
                    } catch (ClassCastException cce) {
                        cce.printStackTrace();
                        throw new InstantiationException("Referenced morpher " + morpherParam.getRef() + " is not a ParameterMorpher.");
                    }
                }
                if (referencedMorpher == null) {
                    Logger.getLogger(getClass()).warn("Referenced parameter morpher " + morpherParam.getRef() + " not found.");
                    continue;
                }
                if (referencedMorpher instanceof MockDataAwareParameterMorpher) {
                    ((MockDataAwareParameterMorpher) referencedMorpher).setMockDataManager(this);
                }
                if (referencedMorpher != null) {
                    paramMorpherList.add(referencedMorpher);
                    String mapId = morpherParam.getId() == null ? generateMorpherId(referencedMorpher) : morpherParam.getId();
                    ParameterMorpher previousMorpher = _parameterMorpherMap.put(mapId, referencedMorpher);
                    if (previousMorpher != null) {
                        Logger.getLogger(getClass()).warn(
                                "Duplicate parameter morpher id of " + mapId + "found. Replacing old morpher in map.");
                    }
                    continue;
                }
            }
            if (morpherParam.getClazz() == null) {
                Logger.getLogger(getClass()).warn(
                        "Parameter morpher param did not specify either a class or ref attribute.");
                return null;
            }
            ParameterMorpher morpher = (ParameterMorpher) Class.forName(morpherParam.getClazz()).newInstance();
            if (morpher instanceof MockDataAwareParameterMorpher) {
                ((MockDataAwareParameterMorpher) morpher).setMockDataManager(this);
            }
            morpher.setInitParams(morpherParam.getParam());
            String mapId = morpherParam.getId() == null ? generateMorpherId(morpher) : morpherParam.getId();
            ParameterMorpher previousMorpher = _parameterMorpherMap.put(mapId, morpher);
            if (previousMorpher != null) {
                Logger.getLogger(getClass()).warn(
                        "Duplicate parameter morpher id of " + mapId + "found. Replacing old morpher in map.");
            }
            if (morpher != null) {
                paramMorpherList.add(morpher);
            }
        }
        return paramMorpherList;
    }

}
