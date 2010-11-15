package org.mdf.mockdata.capture;

import java.io.File;
import java.io.FileFilter;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.easymock.EasyMock;
import org.mdf.mockdata.ReflectionParamBuilderUtil3;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class CaptureAuthenticationProviderTest {
    
    private AuthenticationProvider realAuthenticationProviderOneTest;
    private AuthenticationProvider realAuthenticationProviderThreeTests;
    private AuthenticationProvider realAuthenticationProviderThrowingException;
    
    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
    private CaptureAuthenticationProvider captureAuthenticationProviderOneTest;
    private CaptureAuthenticationProvider captureAuthenticationProviderThreeTests;
    private CaptureAuthenticationProvider captureAuthenticationProviderThrowingException;
    
    private Authentication authentication2Roles;
    
    private File scratchDir;

    private File captureAuthenticationXml1;
    private File captureAuthenticationXml2;
    private File captureAuthenticationXml3;
    private File captureAuthenticationXml;



    @BeforeMethod
    public void setUp() throws Exception {
        initialize();
        createScratchDir();

        CaptureAuthenticationProvider.resetTestData();

        captureAuthenticationXml = new File(scratchDir, "CaptureAuthentication.xml");
        captureAuthenticationXml1 = new File(scratchDir, "CaptureAuthentication1.xml");
        captureAuthenticationXml2 = new File(scratchDir, "CaptureAuthentication2.xml");
        captureAuthenticationXml3 = new File(scratchDir, "CaptureAuthentication3.xml");
    }

    @AfterMethod
    public void tearDown() {
        deleteScratchDir();
    }

    private void deleteScratchDir() {
        if(scratchDir != null) {
            for (File file : scratchDir.listFiles()) {
                if(!file.delete()) {
                    AssertJUnit.fail("Could not delete files in scratch directory");
                }
            }
            if(!scratchDir.delete()) {
                AssertJUnit.fail("Could not delete scratch directory");
            }
        }
    }

    private void createScratchDir() throws Exception {
        File rootDir = File.createTempFile("test", null);
        scratchDir = new File(rootDir.getParentFile(), this.getClass().getSimpleName());
        if(!scratchDir.exists() && !scratchDir.mkdir()) {
            AssertJUnit.fail("Could not create scratch directory");
        }
    }

    private void initialize() {
        initializeUsernameAndPasswordToken();

        initializeAuthenticationResponse2Roles();

        initializeRealAuthenticationProviderOneTest();

        initializeRealAuthenticationProviderThreeTests();

        initializeRealAuthenticationProviderThrowingException();

        ititializeCaptureAuthenticationProviderOneTest();

        initializeCaptureAuthenticationProviderThreeTests();

        initializeCaptureAuthenticationProviderThrowingException();
    }

    private void initializeRealAuthenticationProviderThrowingException() {
        realAuthenticationProviderThrowingException = EasyMock.createMock(AuthenticationProvider.class);
        //noinspection ThrowableInstanceNeverThrown
        EasyMock.expect(realAuthenticationProviderThrowingException.authenticate(usernamePasswordAuthenticationToken)).andThrow(new BadCredentialsException("Test")).once();
        EasyMock.replay(realAuthenticationProviderThrowingException);
    }

    private void initializeCaptureAuthenticationProviderThrowingException() {
        captureAuthenticationProviderThrowingException = new CaptureAuthenticationProvider(realAuthenticationProviderThrowingException);
    }

    private void initializeRealAuthenticationProviderThreeTests() {
        realAuthenticationProviderThreeTests = EasyMock.createMock(AuthenticationProvider.class);
        EasyMock.expect(realAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken)).andReturn(authentication2Roles).times(3);
        EasyMock.replay(realAuthenticationProviderThreeTests);
    }

    private void initializeCaptureAuthenticationProviderThreeTests() {
        captureAuthenticationProviderThreeTests = new CaptureAuthenticationProvider(realAuthenticationProviderThreeTests);
    }

    private void initializeUsernameAndPasswordToken() {
        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("testuser", "testpassword", new GrantedAuthority[]{});
    }

    private void initializeRealAuthenticationProviderOneTest() {
        realAuthenticationProviderOneTest = EasyMock.createMock(AuthenticationProvider.class);
        EasyMock.expect(realAuthenticationProviderOneTest.authenticate(usernamePasswordAuthenticationToken)).andReturn(authentication2Roles).once();
        EasyMock.replay(realAuthenticationProviderOneTest);
    }

    private void ititializeCaptureAuthenticationProviderOneTest() {
        captureAuthenticationProviderOneTest = new CaptureAuthenticationProvider(realAuthenticationProviderOneTest);
    }

    private void initializeAuthenticationResponse2Roles() {
        authentication2Roles =
                new UsernamePasswordAuthenticationToken(
                        usernamePasswordAuthenticationToken.getPrincipal(),
                        usernamePasswordAuthenticationToken.getCredentials(),
                        new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_TEST1"),  new GrantedAuthorityImpl("ROLE_TEST2") }
                );
    }

    @Test
    public void getTestDataDoesNotReturnNull() {
        MockData testData = CaptureAuthenticationProvider.getTestData();
        AssertJUnit.assertNotNull(testData);
    }

    @Test
    public void getTestDataCategoryIsAuthentication() {
        MockData testData = CaptureAuthenticationProvider.getTestData();
        String categoryName = testData.getCategory(0).getName();
        AssertJUnit.assertEquals("authentication", categoryName);
    }

    @Test
    public void usernamePasswordTokenAuthenticationIsSupported() {
         AssertJUnit.assertTrue(
            "UsernamePasswordAuthenticationToken must be supported but is not",
            captureAuthenticationProviderOneTest.supports(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test(
        expectedExceptions = IllegalArgumentException.class
    )
    public void realAuthenticationProviderMustNotBeNull() {
        new CaptureAuthenticationProvider(null);
    }

    @Test
    public void realAuthenticationProviderIsInvoked() {
        captureAuthenticationProviderOneTest.authenticate(usernamePasswordAuthenticationToken);

        EasyMock.verify(realAuthenticationProviderOneTest);
    }

    @Test
    public void authenticationFromRealProviderIsReturned() {
        Authentication authentication = captureAuthenticationProviderOneTest.authenticate(usernamePasswordAuthenticationToken);

        AssertJUnit.assertEquals(authentication2Roles, authentication);
    }

    @Test
    public void whenWriteEachRequestIsFalse() {
        captureAuthenticationProviderOneTest.setWriteEachRequest(false);

        captureAuthenticationProviderOneTest.authenticate(usernamePasswordAuthenticationToken);

        MockData testData = CaptureAuthenticationProvider.getTestData();

        AssertJUnit.assertEquals(1, testData.getCategoryCount());
        AssertJUnit.assertEquals("authentication", testData.getCategory(0).getName());
        AssertJUnit.assertEquals(1, testData.getCategory(0).getTestCount());
    }

    @Test
    public void requestPortionOfTestGetsBuiltFromInputUsernamePasswordAuthenticationToken() throws Exception {
        captureAuthenticationProviderOneTest.setWriteEachRequest(false);

        captureAuthenticationProviderOneTest.authenticate(usernamePasswordAuthenticationToken);

        MockData testData = CaptureAuthenticationProvider.getTestData();

        Request request = testData.getCategory(0).getTest(0).getRequest();
        AssertJUnit.assertEquals(1, request.getParamCount());

        AssertJUnit.assertEquals("authenticationRequest", request.getParam(0).getName());

        UsernamePasswordAuthenticationToken actualAuthenticationRequest =
                ReflectionParamBuilderUtil3.buildObjectFromParams(request.getParam(0), UsernamePasswordAuthenticationToken.class);

        AssertJUnit.assertEquals(usernamePasswordAuthenticationToken.getPrincipal(), actualAuthenticationRequest.getPrincipal());
        AssertJUnit.assertEquals(usernamePasswordAuthenticationToken.getCredentials(), actualAuthenticationRequest.getCredentials());
        AssertJUnit.assertEquals(usernamePasswordAuthenticationToken.getAuthorities().length, actualAuthenticationRequest.getAuthorities().length);
        AssertJUnit.assertEquals(usernamePasswordAuthenticationToken.getDetails(), actualAuthenticationRequest.getDetails());
        AssertJUnit.assertEquals(usernamePasswordAuthenticationToken.getName(), actualAuthenticationRequest.getName());        
    }

    @Test
    public void responsePortionOfTestGetsBuiltFromResultAuthentication() throws Exception {
        captureAuthenticationProviderOneTest.setWriteEachRequest(false);

        captureAuthenticationProviderOneTest.authenticate(usernamePasswordAuthenticationToken);

        MockData testData = CaptureAuthenticationProvider.getTestData();

        Response response = testData.getCategory(0).getTest(0).getResponse();

        AssertJUnit.assertEquals(1, response.getParamCount());

        AssertJUnit.assertEquals("authenticationResponse", response.getParam(0).getName());

        UsernamePasswordAuthenticationToken actualAuthenticationResponse =
                ReflectionParamBuilderUtil3.buildObjectFromParams(response.getParam(0), UsernamePasswordAuthenticationToken.class);

        AssertJUnit.assertEquals(authentication2Roles.getPrincipal(), actualAuthenticationResponse.getPrincipal());
        AssertJUnit.assertEquals(authentication2Roles.getCredentials(), actualAuthenticationResponse.getCredentials());
        AssertJUnit.assertEquals(authentication2Roles.getAuthorities().length, actualAuthenticationResponse.getAuthorities().length);
        AssertJUnit.assertEquals(authentication2Roles.getDetails(), actualAuthenticationResponse.getDetails());
        AssertJUnit.assertEquals(authentication2Roles.getName(), actualAuthenticationResponse.getName());         
    }

    @Test
    public void writeTestDataCreatesFile() throws Exception {
        captureAuthenticationProviderOneTest.setWriteEachRequest(false);

        captureAuthenticationProviderOneTest.setDumpDir(scratchDir.getAbsolutePath());

        captureAuthenticationProviderOneTest.authenticate(usernamePasswordAuthenticationToken);

        captureAuthenticationProviderOneTest.writeTestData();
        
        assertFileExists(captureAuthenticationXml);
    }

    @Test
    public void writeEachRequestFalseDoesNotCreateFilesOnAuthenticate() {
        captureAuthenticationProviderThreeTests.setWriteEachRequest(false);
        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);
        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);
        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);
        
        File[] captureFiles = scratchDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().contains("CaptureAuthentication");
            }
        });

        int numberOfCaptureFiles = captureFiles.length;

        AssertJUnit.assertEquals("No files should be written on call to authenticate when writeEachRequest = false", 0, numberOfCaptureFiles);
    }


    @Test
    public void writeEachRequestFalseKeepsAddingTestsToGlobalTestData() {
        captureAuthenticationProviderThreeTests.setWriteEachRequest(false);
        
        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);
        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);
        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);

        MockData testData = CaptureAuthenticationProvider.getTestData();
        int numberOfTestsStored = testData.getCategory(0).getTestCount();

        AssertJUnit.assertEquals(3, numberOfTestsStored);
    }

    @Test
    public void writeEachRequest() {

        captureAuthenticationProviderOneTest.setWriteEachRequest(true);

        captureAuthenticationProviderOneTest.setDumpDir(scratchDir.getAbsolutePath());

        captureAuthenticationProviderOneTest.authenticate(usernamePasswordAuthenticationToken);

        assertFileExists(captureAuthenticationXml1);
    }

    @Test
    public void writeEachRequestMultipleCaptures() {

        captureAuthenticationProviderThreeTests.setWriteEachRequest(true);

        captureAuthenticationProviderThreeTests.setDumpDir(scratchDir.getAbsolutePath());

        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);
        assertFileExists(captureAuthenticationXml1);

        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);
        assertFileExists(captureAuthenticationXml2);

        captureAuthenticationProviderThreeTests.authenticate(usernamePasswordAuthenticationToken);
        assertFileExists(captureAuthenticationXml3);
    }

    private void assertFileExists(File f) {
        AssertJUnit.assertTrue("Expected file " + f + " to exist, but it does not", f.exists());
    }

    @Test(expectedExceptions = BadCredentialsException.class)
    public void authenticationExceptionDuringAuthenticateGetsRethrown() {
        captureAuthenticationProviderThrowingException.setWriteEachRequest(false);
        captureAuthenticationProviderThrowingException.authenticate(usernamePasswordAuthenticationToken);
    }

    @Test
    public void authenticationExceptionDuringAuthenticateIsAddedToTest() {
        captureAuthenticationProviderThrowingException.setWriteEachRequest(false);

        try {
            captureAuthenticationProviderThrowingException.setDumpDir(scratchDir.getAbsolutePath());
            captureAuthenticationProviderThrowingException.authenticate(usernamePasswordAuthenticationToken);
        }
        catch (AuthenticationException ignored) {
        }

        Category category = CaptureAuthenticationProvider.getTestData().getCategory(0);

        AssertJUnit.assertEquals(1, category.getTestCount());

        Response response = category.getTest(0).getResponse();

        AssertJUnit.assertEquals(1, response.getParamCount());

        Param exceptionParam = response.getParam(0);

        AssertJUnit.assertEquals("exception", exceptionParam.getName());
    }


}
