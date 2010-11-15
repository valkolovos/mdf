package org.mdf.mockdata.capture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.ReflectionParamBuilderUtil3;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.mdf.mockdata.generated.Test;

public class CaptureAuthenticationProvider implements AuthenticationProvider{
    private static MockData testData;
    private final AuthenticationProvider realAuthenticationProvider;
    private boolean writeEachRequest;
    private String dumpDir;
    private AtomicInteger sequence = new AtomicInteger(1);
    private static final String BASE_FILE_NAME_WITHOUT_EXTENSION = "CaptureAuthentication";
    private static final String BASE_FILE_NAME = BASE_FILE_NAME_WITHOUT_EXTENSION + ".xml";

    public CaptureAuthenticationProvider(AuthenticationProvider realAuthenticationProvider) {      
        if(realAuthenticationProvider == null) {
            throw new IllegalArgumentException("realAuthenticationProvider must not be null");
        }

        this.realAuthenticationProvider = realAuthenticationProvider;
    }

    public synchronized static MockData getTestData() {
        if (testData == null) {
            testData = createMockDataWithAuthenticationCategory();
        }
        return testData;
    }

    private static MockData createMockDataWithAuthenticationCategory() {
        MockData mockData = new MockData();
        Category category = new Category();
        category.setName("authentication");
        mockData.addCategory(category);
        return mockData;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication resultAuthentication;
        try {
            resultAuthentication = realAuthenticationProvider.authenticate(authentication);

            writeTestOrAddItToTestData(authentication, resultAuthentication);
        }
        catch (AuthenticationException e) {
            writeTestOrAddItToTestData(authentication, e);
            throw e;
        }
        
        return resultAuthentication;
    }

    private void writeTestOrAddItToTestData(Authentication authentication, AuthenticationException authenticationException)  {
        if(writeEachRequest) {
            try {
                createAndWriteTest(authentication, authenticationException);
            }   
            catch (MarshalException e) {
                Logger.getLogger(CaptureAuthenticationProvider.class).warn("Unable to capture authentication data", e);
            }
            catch (ValidationException e) {
                Logger.getLogger(CaptureAuthenticationProvider.class).warn("Unable to capture authentication data", e);
            }
            catch (IOException e) {
                Logger.getLogger(CaptureAuthenticationProvider.class).warn("Unable to capture authentication data", e);
            }
        }
        else {
            createAndAddTest(authentication, authenticationException);
        }
    }

    private void createAndAddTest(Authentication authentication, AuthenticationException e) {
        Test test = createTestWithExceptionResponse(authentication, e);
        addTest(test);
    }



    private void createAndWriteTest(Authentication authentication, AuthenticationException e) throws MarshalException, IOException, ValidationException {
        Test test = createTestWithExceptionResponse(authentication, e);
        writeTest(test);
    }

    private void writeTestOrAddItToTestData(Authentication authentication, Authentication resultAuthentication) {
        if(writeEachRequest) {
            try {
                createAndWriteTest(authentication, resultAuthentication);
            }
            catch (MarshalException e) {
                Logger.getLogger(CaptureAuthenticationProvider.class).warn("Unable to capture authentication data", e);
            }
            catch (ValidationException e) {
                Logger.getLogger(CaptureAuthenticationProvider.class).warn("Unable to capture authentication data", e);
            }
            catch (IOException e) {
                Logger.getLogger(CaptureAuthenticationProvider.class).warn("Unable to capture authentication data", e);
            }
        }
        else {
            createAndAddTest(authentication, resultAuthentication);
        }
    }

    private void createAndWriteTest(Authentication authentication, Authentication resultAuthentication) throws MarshalException, IOException, ValidationException {
        Test test = createTestWithAuthenticationResponse(authentication, resultAuthentication);
        writeTest(test);
    }

    private void writeTest(Test test) throws MarshalException, ValidationException, IOException {
        
        MockData mockData = createMockDataWithAuthenticationCategory();
        mockData.getCategory(0).addTest(test);

        String fileName =
                new StringBuilder(BASE_FILE_NAME_WITHOUT_EXTENSION)
                        .append(sequence.getAndIncrement())
                        .append(".xml").toString();

        String dumpDir = this.dumpDir == null ? System.getProperty("orbitz.server.log.dir") : this.dumpDir;
        MockDataManager.marshallTestData(mockData, dumpDir + File.separator + fileName, false);
    }


    private static void createAndAddTest(Authentication authentication, Authentication resultAuthentication) {
        Test test = createTestWithAuthenticationResponse(authentication, resultAuthentication);
        addTest(test);
    }

    private static Test createTestWithExceptionResponse(Authentication authentication, AuthenticationException e) {
        return createTest(authentication, e);
    }

    private static Test createTestWithAuthenticationResponse(Authentication authentication, Authentication resultAuthentication) {
        return createTest(authentication, resultAuthentication);
    }

    private static Test createTest(Authentication authentication, Object result) {
        Test test = new Test();

        Request request = createRequest(authentication);

        test.setRequest(request);

        Response response = createResponse(result);

        test.setResponse(response);
        return test;
    }


    private static void addTest(Test test) {
        getTestData().getCategory(0).addTest(test);
    }

    private static Response createResponse(Object result) {
        Response response = new Response();
        Param authenticationParam = new Param();

        if(result instanceof Throwable) {
            authenticationParam.setName("exception");
        }
        else {
            authenticationParam.setName("authenticationResponse");
        }

        try {
            ReflectionParamBuilderUtil3.buildParamFromObject(result, authenticationParam, true);
        }
        catch(Exception e) {
            throw new RuntimeException("failed to build parameter from result authentication", e);
        }

        response.addParam(authenticationParam);
        return response;
    }

    private static Request createRequest(Authentication authentication) {
        Request request = new Request();

        Param requestParam = new Param();
        try {
            requestParam.setName("authenticationRequest");
            ReflectionParamBuilderUtil3.buildParamFromObject(authentication, requestParam, true);
        } catch (Exception e) {
            throw new RuntimeException("failed to build parameter from request authentication", e);
        }

        request.addParam(requestParam);

        return request;
    }

    public boolean supports(Class authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class); 
    }

    public void setWriteEachRequest(boolean writeEachRequest) {
        this.writeEachRequest = writeEachRequest;
    }

    public void setDumpDir(String dumpDir) {
        this.dumpDir = dumpDir;
    }

    public void writeTestData() throws MarshalException, ValidationException, IOException {

        String dumpDir = this.dumpDir == null ? System.getProperty("orbitz.server.log.dir") : this.dumpDir;
        MockDataManager.marshallTestData(testData, dumpDir + File.separator + BASE_FILE_NAME, false);
    }

    public static synchronized void resetTestData() {
        testData = createMockDataWithAuthenticationCategory();
    }
}
