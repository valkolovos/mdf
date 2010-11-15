package org.mdf.mockdata;

import org.springframework.beans.factory.config.AbstractFactoryBean;


public class MockDriverFactoryBean extends AbstractFactoryBean {
    
    private MockDataManager _mockDataManager;

    @SuppressWarnings("unchecked")
    public Class getObjectType() {
        return MockDriver.class;
    }

    protected Object createInstance() throws Exception {
        MockDriver mockDriver = MockDriver.getInstance();
        mockDriver.setMockDataManager(_mockDataManager);
        return mockDriver;
    }
    
    public void setMockDataManager(MockDataManager mockDataManager) {
        _mockDataManager = mockDataManager;
    }

}
