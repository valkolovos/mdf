package org.mdf.mockdata;

import java.util.List;

import org.mdf.mockdata.generated.Param;


/**
 * A ParameterMorpher allows custom runtime manipulation of test data requests and responses
 * to meet the test data needs. For example, date data may need to be
 * time-shifted to meet the expected response. A custom ParameterMorpher can be
 * created to support that requirement.
 */
public interface ParameterMorpher {
    /**
     * This method is called by the MockDataManager with the parameter name from
     * TestData. If this ParameterMorpher can handle the parameter, this method
     * returns true.
     * 
     * @param categoryName The category name of the test
     * @param parameterName
     *            The parameter name to check against.
     * @return true if this ParameterMorpher can handle the given parameter
     */
    boolean canMorphParameter(String categoryName, String parameterName);

    /**
     * Morphs the parameter into the expected value.
     * 
     * @param param
     *            The parameter from TestData
     * @return A list of ParamType objects that conform to the expected
     *         value.
     * @throws Exception
     */
    List<Param> morphParameter(Param param) throws Exception;
    
    /**
     * Sets the initialization parameters for the morpher. Used by MockDataManager
     * when loading mock data from a file.
     * @param initParams List of parameters read from Mock Data file
     * @since 3.0
     */
    void setInitParams(Param... initParams);
}
