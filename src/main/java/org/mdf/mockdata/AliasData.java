/**
 * 
 */
package org.mdf.mockdata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AliasData {
    private Map<String, Integer> orderMap = new HashMap<String, Integer>();
    private Map<String, Set<String>> columnAliases = new HashMap<String, Set<String>>();
    private Map<String, String> tableAliases = new HashMap<String, String>();
    private Set<String> realTableNames = new HashSet<String>();
    private boolean unambiguousColumnsOk = true;
    
    public Map<String, Integer> getOrderMap() {
        return orderMap;
    }
    public Map<String, Set<String>> getColumnAliases() {
        return columnAliases;
    }
    public Map<String, String> getTableAliases() {
        return tableAliases;
    }
    public Set<String> getRealTableNames() {
        return realTableNames;
    }
    public boolean isUnambiguousColumnsOk() {
        return unambiguousColumnsOk;
    }
    public void setUnambiguousColumnsOk(boolean unambiguousColumnsOk) {
        this.unambiguousColumnsOk = unambiguousColumnsOk;
    }
}