package org.mdf.mockdata;

public interface MockDataAwareParameterMatcher extends ParameterMatcher {
    void setMockDataManager(MockDataManager mockDataManager);
}
