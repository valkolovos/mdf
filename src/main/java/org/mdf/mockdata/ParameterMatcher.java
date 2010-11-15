package org.mdf.mockdata;

import java.util.List;

import org.mdf.mockdata.generated.Param;


/**
 * A ParameterMatcher can be used to customize parameter matching logic
 * in the MockDataFramework. For example, some parameters might be date or
 * time sensitive. In this case, a custom ParameterMatcher can be used to
 * validate that the given request parameters match the test parameters by
 * using variable date information.
 */
public interface ParameterMatcher {
    
    /**
     * Checks to see if the given request parameters match the test parameters.
     * @param requestParams The request parameters
     * @param testParams The test parameters from {@link TestData}
     * @return true if the request parameters match the test parameters
     */
    boolean paramsMatch(List<Param> requestParams, Param[] testParams);

    /**
     * Sets the initialization parameters for the matcher. Used by MockDataManager
     * when loading mock data from a file.
     * @param initParams List of parameters read from Mock Data file
     * @since 3.0
     */
    void setInitParams(Param... initParams);
}
