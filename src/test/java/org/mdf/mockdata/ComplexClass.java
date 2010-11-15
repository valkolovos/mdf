/**
 * 
 */
package org.mdf.mockdata;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ComplexClass implements Serializable {
    private String stringField;
    private Map<String, ComplexClass2> mapField;
    private long[] longArray;
    private ComplexClass child;
    private List<Boolean> booleanList;
    private ComplexClass sameChild;
    private Object nullObject;
    private ComplexClass2 _c2;

    public ComplexClass(Map<String, ComplexClass2> m, boolean initChild) {
        if (m == null) {
            throw new IllegalArgumentException("Map must be provided");
        }
        stringField = "stringField";
        mapField = m;
        mapField.put("key1", new ComplexClass2("value1"));
        longArray = new long[] { 1, 2, 3 };
        booleanList = new LinkedList<Boolean>();
        booleanList.add(true);
        booleanList.add(false);
        if (initChild) {
            child = new ComplexClass(m, false);
            sameChild = child;
            child.stringField = "childStringField";
        }
    }
    
    public ComplexClass getChild() {
        return child;
    }
    
    public List<Boolean> getBooleanList() {
        return booleanList;
    }

    public Map<String, ComplexClass2> getMapField() {
        return mapField;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ComplexClass)) {
            return false;
        }
        ComplexClass cc = (ComplexClass) o;
        EqualsBuilder eb = new EqualsBuilder();
        eb.append(stringField, cc.stringField);
        if (!eb.isEquals()) return false;
        eb.append(mapField, cc.mapField);
        if (!eb.isEquals()) return false;
        eb.append(longArray, cc.longArray);
        if (!eb.isEquals()) return false;
        eb.append(child, cc.child);
        if (!eb.isEquals()) return false;
        eb.append(booleanList, cc.booleanList);
        if (!eb.isEquals()) return false;
        eb.append(nullObject, cc.nullObject);
        if (!eb.isEquals()) return false;
        return true;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(stringField).append(mapField).append(longArray).append(child).append(
                booleanList).append(nullObject).toHashCode();
    }
}