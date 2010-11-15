package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.mdf.mockdata.generated.Param;
import org.testng.annotations.Test;

public class SqlSnippetMatcherTest {
    
    @Test()
    @SuppressWarnings("unchecked")
    public void testParamsMatchContains() throws Exception {
        SqlSnippetMatcher matcher = new SqlSnippetMatcher();
        Param sqlParam = new Param();
        sqlParam.setName("sql");
        sqlParam.setValue("select * from table foo where foo.id = 'bar'");
        Param sqlSnippetParam = new Param();
        sqlSnippetParam.setName("sqlSnippet");
        sqlSnippetParam.setValue("FROM table foo");
        assertTrue(matcher.paramsMatch(Arrays.asList(new Param[] { sqlParam }), new Param[] { sqlSnippetParam }));
    }
    
    @Test()
    @SuppressWarnings("unchecked")
    public void testParamsMatchRegEx() throws Exception {
        SqlSnippetMatcher matcher = new SqlSnippetMatcher();
        Param initParam = new Param();
        initParam.setName("defaultParameterName");
        initParam.setValue("SomethingOtherThanSqlSnippet");
        matcher.setInitParams(initParam);
        Param sqlParam = new Param();
        sqlParam.setName("sql");
        sqlParam.setValue("select * from table   foo where foo.id = 'bar'");
        Param sqlSnippetParam = new Param();
        sqlSnippetParam.setName("SomethingOtherThanSqlSnippet");
        sqlSnippetParam.setValue("FROM table *foo .*id =");
        assertTrue(matcher.paramsMatch(Arrays.asList(new Param[] { sqlParam }), new Param[] { sqlSnippetParam }));
    }
}
