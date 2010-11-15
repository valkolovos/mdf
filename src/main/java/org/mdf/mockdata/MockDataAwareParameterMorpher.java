package org.mdf.mockdata;

public interface MockDataAwareParameterMorpher extends ParameterMorpher {
    void setMockDataManager(MockDataManager mockDataManager);
}
