package org.mdf.mockdata;

public interface DelegatingParameterMatcher extends ParameterMatcher {
    void setDelegateParameterMatcher(ParameterMatcher delegateParameterMatcher);
}
