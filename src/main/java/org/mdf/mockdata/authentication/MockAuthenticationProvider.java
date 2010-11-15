package org.mdf.mockdata.authentication;

import java.util.Collections;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.ReflectionParamBuilderUtil3;
import org.mdf.mockdata.generated.Param;

public class MockAuthenticationProvider implements AuthenticationProvider {
    private final MockDataManager mockDataManager;

    public MockAuthenticationProvider(MockDataManager mockDataManager) {   
        if(mockDataManager == null) {
            throw new IllegalArgumentException("mockDataManager must not be null");
        }
        this.mockDataManager = mockDataManager;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Param authenticationParam = createAuthenticationRequestParam(authentication);

        Param[] responseParam = matchRequestAndBuildResponseParam(authenticationParam);

        Object response = convertParamIntoResponseObject(responseParam);

        if(response instanceof AuthenticationException) {
            throw (AuthenticationException)response;
        }

        return (Authentication) response;  
    }

    private Object convertParamIntoResponseObject(Param[] responseParam) {
        Object response = null;

        if(responseParam != null && responseParam.length != 0 &&responseParam[0] != null) {
            String implementationType = findImplementationType(responseParam[0]);
            try {
                response = ReflectionParamBuilderUtil3.buildObjectFromParams(responseParam[0], Class.forName(implementationType));
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to create responseParam authentication", e);
            }
        }
        return response;
    }

    private Param[] matchRequestAndBuildResponseParam(Param authenticationParam) {
        Param[] responseParam;

        try {
            responseParam = mockDataManager.findResponse(Collections.<Param>singletonList(authenticationParam));
        }
        catch (Exception e) {
            throw new RuntimeException("findResponse failed unexpectedly", e);
        }
        return responseParam;
    }

    private Param createAuthenticationRequestParam(Authentication authentication) {
        Param authenticationParam = new Param();

        try {
            ReflectionParamBuilderUtil3.buildParamFromObject(authentication, authenticationParam, true);
            authenticationParam.setName("authenticationRequest");
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to convert request authentication to Param", e);
        }
        return authenticationParam;
    }

    private String findImplementationType(Param response) {
        String implementationType = null;

        for (Param param : response.getParam()) {
            if("Implementation Type".equals(param.getName())) {
                implementationType = param.getValue();
            }
        }
        return implementationType;
    }

    public boolean supports(Class authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
