package org.mdf.authentication;

import org.acegisecurity.Authentication;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.ReflectionParamBuilderUtil3;
import org.mdf.mockdata.authentication.MockAuthenticationProvider;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class MockAuthenticationProviderTest {
    private MockData testData;
    private MockDataManager mockDataManager;
    private MockAuthenticationProvider mockAuthenticationProvider;
    private org.mdf.mockdata.generated.Test successfulLoginTest;
    private org.mdf.mockdata.generated.Test failedLoginTest;
    private Param errorAuthenticationRequestParam;
    private UsernamePasswordAuthenticationToken errorRequestAuthentication;
    private UsernamePasswordAuthenticationToken successfulRequestAuthentication;
    private Param successfulAuthenticationRequestParam;
    private UsernamePasswordAuthenticationToken successfulResponseAuthentication;
    private Param successfulAuthenticationResponseParam;
    private Param errorAuthenticationResponseParam;
    private UsernamePasswordAuthenticationToken noMatchRequestAuthentication;

    @BeforeMethod
    public void setUp() throws Exception {
        initializeMockData();
        initializeMockDataManager();
        initializeMockAuthenticationProvider();
        initializeNoMatchRequestAuthentication();
    }

    private void initializeNoMatchRequestAuthentication() {
        noMatchRequestAuthentication = new UsernamePasswordAuthenticationToken("nomatch", "nomatch");
    }

    private void initializeMockAuthenticationProvider() {
        mockAuthenticationProvider = new MockAuthenticationProvider(mockDataManager);
    }

    private void initializeMockDataManager() {
        mockDataManager = new MockDataManager(testData);
    }

    private void initializeMockData() throws Exception {
        testData = new MockData();
        Category category = new Category();
        category.setName("authentication");

        initializeSuccessfulLoginTest();

        initializeFailedLoginTest();
        
        category.addTest(successfulLoginTest);
        category.addTest(failedLoginTest);

        testData.addCategory(category);
    }

    private void initializeFailedLoginTest() throws Exception {
        failedLoginTest = new org.mdf.mockdata.generated.Test();

        Request request = new Request();
        initializeErrorAuthenticationRequestParam();
        request.addParam(errorAuthenticationRequestParam);

        Response response = new Response();
        intializeErrorAuthenticationResponseParam();
        response.addParam(errorAuthenticationResponseParam);

        failedLoginTest.setRequest(request);
        failedLoginTest.setResponse(response);
    }

    private void intializeErrorAuthenticationResponseParam() throws Exception {
        errorAuthenticationResponseParam = new Param();
        @SuppressWarnings({"ThrowableInstanceNeverThrown"}) BadCredentialsException exception = new BadCredentialsException("BAD CREDENTIALS");
        exception.fillInStackTrace();
        ReflectionParamBuilderUtil3.buildParamFromObject(exception, errorAuthenticationResponseParam, true);
        errorAuthenticationResponseParam.setName("exception");
    }

    private void initializeErrorAuthenticationRequestParam() throws Exception {
        errorAuthenticationRequestParam = new Param();
        errorAuthenticationRequestParam.setName("authenticationRequest");
        initializeErrorAuthenticationToken();
        ReflectionParamBuilderUtil3.buildParamFromObject(errorRequestAuthentication, errorAuthenticationRequestParam, true);
    }

    private void initializeErrorAuthenticationToken() {
        errorRequestAuthentication = new UsernamePasswordAuthenticationToken("erroruser", "errorpassword");
        errorRequestAuthentication.setAuthenticated(false);
    }

    private void initializeSuccessfulLoginTest() throws Exception {
        successfulLoginTest = new org.mdf.mockdata.generated.Test();

        Request request = createSuccessfulLoginRequest();

        Response response = createSuccessfulLoginResponse();

        successfulLoginTest.setRequest(request);
        successfulLoginTest.setResponse(response);
    }

    private Response createSuccessfulLoginResponse() throws Exception {
        Response response = new Response();
        initializeSuccessfulAuthenticationResponseParam();
        response.addParam(successfulAuthenticationResponseParam);
        return response;
    }

    private Request createSuccessfulLoginRequest() throws Exception {
        Request request = new Request();
        initializeSuccessfulAuthenticationRequestParam();
        request.addParam(successfulAuthenticationRequestParam);
        return request;
    }

    private void initializeSuccessfulAuthenticationResponseParam() throws Exception {
        successfulAuthenticationResponseParam = new Param();
        successfulAuthenticationResponseParam.setName("authenticationResponse");
        initializeSuccessfulResponseAuthentication(successfulAuthenticationResponseParam);
    }

    private void initializeSuccessfulResponseAuthentication(Param authenticationResponseParam) throws Exception {
        successfulResponseAuthentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "testpassword",
                new GrantedAuthority[] {new GrantedAuthorityImpl("TEST_ROLE_1"), new GrantedAuthorityImpl("TEST_ROLE_2")});
        ReflectionParamBuilderUtil3.buildParamFromObject(successfulResponseAuthentication, authenticationResponseParam, true);
    }

    private void initializeSuccessfulAuthenticationRequestParam() throws Exception {
        successfulAuthenticationRequestParam = new Param();
        successfulAuthenticationRequestParam.setName("authenticationRequest");
        initializeSuccessfulAuthenticationToken();
        ReflectionParamBuilderUtil3.buildParamFromObject(successfulRequestAuthentication, successfulAuthenticationRequestParam, true);
    }

    private void initializeSuccessfulAuthenticationToken() {
        successfulRequestAuthentication = new UsernamePasswordAuthenticationToken("testuser", "testpassword");
        successfulRequestAuthentication.setAuthenticated(false);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void mockDataManagerMustNotBeNull() {
        new MockAuthenticationProvider(null);
    }

    @Test
    public void authenticationByUsernamePasswordAuthenticationTokenIsSupported() {
         AssertJUnit.assertTrue(
            "UsernamePasswordAuthenticationToken must be supported but is not",
            mockAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test(expectedExceptions = BadCredentialsException.class)
    public void exceptionMatchesThrowTheException() {
        mockAuthenticationProvider.authenticate(errorRequestAuthentication);
    }

    @Test
    public void successfulMatchReturnsTheResult() {
        Authentication responseAuthentication = mockAuthenticationProvider.authenticate(successfulRequestAuthentication);
        AssertJUnit.assertNotNull("Response authentication was null, but it must not be", responseAuthentication);
        compareAuthentications(successfulResponseAuthentication, responseAuthentication);
    }

    @Test
    public void noMatchResturnsNull() {
        AssertJUnit.assertNull(mockAuthenticationProvider.authenticate(noMatchRequestAuthentication));
    }

    private void compareAuthentications(Authentication expectedResponseAuthentication, Authentication actualResponseAuthentication) {
        AssertJUnit.assertEquals(expectedResponseAuthentication.isAuthenticated(), actualResponseAuthentication.isAuthenticated());
        AssertJUnit.assertEquals(expectedResponseAuthentication.getPrincipal(), actualResponseAuthentication.getPrincipal());
        AssertJUnit.assertEquals(expectedResponseAuthentication.getCredentials(), actualResponseAuthentication.getCredentials());

        compareAuthorities(expectedResponseAuthentication.getAuthorities(), actualResponseAuthentication.getAuthorities());
    }

    private void compareAuthorities(GrantedAuthority[] expectedAuthorities, GrantedAuthority[] actualAuthorities) {
        AssertJUnit.assertEquals(expectedAuthorities.length, actualAuthorities.length);
        for (int i = 0; i < expectedAuthorities.length; i++) {
            GrantedAuthority expectedAuthority = expectedAuthorities[i];
            GrantedAuthority actualAuthority = actualAuthorities[i];

            AssertJUnit.assertEquals(expectedAuthority.getAuthority(), actualAuthority.getAuthority());
        }
    }
}
