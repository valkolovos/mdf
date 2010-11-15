package org.mdf.mockdata;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ComplexClass2 implements Serializable {
    private String _stringField;
    
    public ComplexClass2(String stringField) {
        _stringField = stringField;
    }
    
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ComplexClass2)) {
            return false;
        }
        return new EqualsBuilder().append(_stringField, ((ComplexClass2)o)._stringField).isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(_stringField).toHashCode();
    }
}
