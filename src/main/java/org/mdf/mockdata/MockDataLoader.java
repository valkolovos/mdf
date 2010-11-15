package org.mdf.mockdata;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.Import;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Template;
import org.mdf.mockdata.generated.Test;
import org.mdf.mockdata.generated.UseTemplate;
import org.mdf.mockdata.generated.Variable;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class MockDataLoader {

    private Pattern templateRowMatcherPattern = Pattern.compile("(.*)\\[(\\d+)\\]");
    private Map<String, Template> mockDataTemplateMap = new LinkedHashMap<String, Template>();
    private Map<String, String> globalVariableMap = new HashMap<String, String>();

    public MockData loadMockData(String dataFile) throws Exception {
        return loadMockData(dataFile, "org.mdf.mockdata.RuntimeMapping.xml", true);
    }

    /**
     * Sets global variables for variable replacement. This will override any
     * global variables found in the mock data file.
     * 
     * @param globalVariableMap
     *            Map of variables to use as global variables
     * @since 3.3
     */
    public void setGlobalVariableMap(Map<String, String> globalVariableMap) {
        this.globalVariableMap = globalVariableMap;
    }

    protected MockData loadMockData(String dataFile, String runtimeMapping, boolean includeImportedTests)
            throws Exception {
        XMLContext context = new XMLContext();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Mapping mapping = new Mapping();
        mapping.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                String[] fileComponents = systemId.split("\\.");
                String fileName = fileComponents[fileComponents.length - 2] + "."
                        + fileComponents[fileComponents.length - 1];
                String path = systemId.substring(0, systemId.indexOf(fileName));
                path = path.replace(".", File.separator);
                String mappingFile = path + fileName;
                InputSource reader = null;
                if (new File(mappingFile).exists()) {
                    reader = new InputSource(new FileReader(mappingFile));
                } else {
                    // resources always use "/" for separators regardless of OS
                    mappingFile = mappingFile.replace(File.separator, "/");
                    reader = new InputSource(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                            mappingFile)));
                }
                return reader;
            }

        });
        if (runtimeMapping != null) {
            mapping.loadMapping(runtimeMapping);
            unmarshaller.setMapping(mapping);
        }
        unmarshaller.setClass(MockData.class);
        Reader reader = null;
        if (new File(dataFile).exists()) {
            reader = new FileReader(dataFile);
        } else {
            InputStream is = MockDataManager.class.getClassLoader().getResourceAsStream(dataFile);
            if (is == null) {
                throw new Exception("Unable to load " + dataFile + ".");
            }
            reader = new InputStreamReader(MockDataManager.class.getClassLoader().getResourceAsStream(dataFile));
        }
        MockData mockData = (MockData) unmarshaller.unmarshal(reader);
        for (Import importObj : mockData.getImport()) {
            MockData childData = loadMockData(importObj.getFile());
            for (Variable v : childData.getVariable()) {
                mockData.addVariable(v);
            }
            for (Template t : childData.getTemplate()) {
                mockData.addTemplate(t);
            }
            if (includeImportedTests) {
                for (Category c : childData.getCategory()) {
                    mockData.addCategory(c);
                }
            }
        }
        return mockData;
    }

    public void readTestDataFromFile(MockData mockData) throws Exception {
    
        ArrayList<Param> paramListArray = new ArrayList<Param>();
        for (Template template : mockData.getTemplate()) {
            mockDataTemplateMap.put(template.getName(), template);
        }
        for (Variable variable : mockData.getVariable()) {
            if (!globalVariableMap.containsKey(variable.getName())) {
                globalVariableMap.put(variable.getName(), variable.getValue());
            }
        }
    
        for (Category category : mockData.getCategory()) {
            Map<String, Template> categoryTemplateMap = new LinkedHashMap<String, Template>(mockDataTemplateMap);
            Map<String, String> categoryVariableMap = new HashMap<String, String>(globalVariableMap);
            for (Template template : category.getTemplate()) {
                categoryTemplateMap.put(template.getName(), template);
            }
            for (Variable variable : category.getVariable()) {
                categoryVariableMap.put(variable.getName(), variable.getValue());
            }
            resolveTemplates(categoryTemplateMap, categoryVariableMap);
            for (Test test : category.getTest()) {
    
                Param[] requestParams = mockDataLoaderHelper(test.getRequest().getUseTemplate(), test.getRequest()
                        .getParam(), categoryTemplateMap, categoryVariableMap);
                test.getRequest().setParam(requestParams);
    
                paramListArray.clear();
                Param[] responseParams = null;
                try {
                    responseParams = mockDataLoaderHelper(test.getResponse().getUseTemplate(), test.getResponse()
                            .getParam(), categoryTemplateMap, categoryVariableMap);
                } catch (RuntimeException e) {
                    StringWriter sw = new StringWriter();
                    XMLContext context = new XMLContext();
                    context.setProperty("org.exolab.castor.indent", true);
                    Marshaller marshaller = context.createMarshaller();
                    marshaller.setWriter(sw);
                    marshaller.marshal(category);
                    throw new Exception("Invalid configuration in XML\n\t" + sw.toString(), e);
                }
                test.getResponse().setParam(responseParams);
    
            }
            category.setTemplate(new Template[0]);
        }
    }

    // resolves all templates in template map
    public void resolveTemplates(Map<String, Template> categoryTemplateMap, Map<String, String> categoryVariableMap) {
        for (Template template : categoryTemplateMap.values()) {
            for (UseTemplate useTemplate : template.getUseTemplate()) {
                Template oldTemplate = categoryTemplateMap.get(useTemplate.getName());
                Param[] newParams = doTemplateReplacementHelper(useTemplate, oldTemplate, categoryTemplateMap,
                        categoryVariableMap);
                for (Param param : newParams) {
                    template.addParam(param);
                }
            }
            template.setUseTemplate(new UseTemplate[0]);
            for (Param param : template.getParam()) {
                resolveTemplateHelper(param, categoryTemplateMap, categoryVariableMap);
            }
        }
    }

    // recursively resolves templates on a param
    void resolveTemplateHelper(Param param, Map<String, Template> categoryTemplateMap,
            Map<String, String> categoryVariableMap) {
        for (UseTemplate useTemplate : param.getUseTemplate()) {
            Template oldTemplate = categoryTemplateMap.get(useTemplate.getName());
            Param[] newParams = doTemplateReplacementHelper(useTemplate, oldTemplate, categoryTemplateMap,
                    categoryVariableMap);
    
            for (Param newParam : newParams) {
                param.addParam(newParam);
            }
        }
        param.setUseTemplate(new UseTemplate[0]);
        for (Param childParam : param.getParam()) {
            resolveTemplateHelper(childParam, categoryTemplateMap, categoryVariableMap);
        }
    }

    private Param[] mockDataLoaderHelper(UseTemplate[] useTemplates, Param[] oldParamListArray,
            Map<String, Template> categoryTemplateMap, Map<String, String> categoryVariableMap) {
        ArrayList<Param> paramListArray = new ArrayList<Param>();
        paramListArray.addAll(Arrays.asList(oldParamListArray));
        if (useTemplates != null) {
            for (UseTemplate useTemplate : useTemplates) {
                Template template = categoryTemplateMap.get(useTemplate.getName());
                if (template == null) {
                    throw new RuntimeException("Invalid configuration. Template named " + useTemplate.getName()
                            + " not found.");
                }
                Param[] paramsToAdd = doTemplateReplacementHelper(useTemplate, template, categoryTemplateMap,
                        categoryVariableMap);

                paramListArray.addAll(Arrays.asList(paramsToAdd));

                // test.getRequest().setUseTemplate(new UseTemplate[0]);
            }
            oldParamListArray = paramListArray.toArray(new Param[paramListArray.size()]);
        }
        for (Param oldParam : oldParamListArray) {
            doVariableReplacement(oldParam, categoryVariableMap);
            Param[] newChildParams = mockDataLoaderHelper(oldParam.getUseTemplate(), oldParam.getParam(),
                    categoryTemplateMap, categoryVariableMap);
            oldParam.setParam(newChildParams);
        }
        return oldParamListArray;

    }

    private Param[] doTemplateReplacementHelper(UseTemplate useTemplate, Template oldtemplate,
            Map<String, Template> categoryTemplateMap, Map<String, String> categoryVariableMap) {
        List<Param> newParams = deepCopyParams(oldtemplate.getParam(), categoryVariableMap);
        for (Param incomingParam : useTemplate.getParam()) {
            doVariableReplacement(incomingParam, categoryVariableMap);
            for (UseTemplate paramUseTemplate : incomingParam.getUseTemplate()) {
                Template template = categoryTemplateMap.get(paramUseTemplate.getName());
                if (template == null) {
                    throw new RuntimeException("Invalid configuration. Template named " + paramUseTemplate.getName()
                            + " not found.");
                }
                Param[] foo = doTemplateReplacementHelper(paramUseTemplate, template, categoryTemplateMap,
                        categoryVariableMap);
                for (Param p : foo) {
                    incomingParam.addParam(p);
                }
            }
            Param originalParam = getParam(incomingParam.getName(), newParams);
            if (originalParam != null) {
                originalParam.setValue(incomingParam.getValue());
                if (incomingParam.getParamCount() > 0)
                    originalParam.setParam(incomingParam.getParam());
            } else {
                if (useTemplate.isAppendParams()) {
                    newParams.add(incomingParam);
                }
            }
        }
        return newParams.toArray(new Param[newParams.size()]);
    }

    private void doVariableReplacement(Param param, Map<String, String> variableMap) {
        if (param.getName().contains("@")) {
            param.setName(getVarReplacement(param.getName(), variableMap));
        }
        if (param.getValue() != null && param.getValue().contains("@")) {
            param.setValue(getVarReplacement(param.getValue(), variableMap));
        }
    }

    private String getVarReplacement(String var, Map<String, String> variableMap) {
        String prefix = var.substring(0, var.indexOf("@"));
        String varName = var.substring(var.indexOf("@") + 1);
        String remainder = "";
        if (varName.contains("@")) {
            remainder = varName.substring(varName.indexOf("@") + 1);
            varName = varName.substring(0, varName.indexOf("@"));
        }
        if (variableMap.get(varName) != null) {
            return new StringBuilder(prefix).append(variableMap.get(varName)).append(remainder).toString();
        } else {
            Logger.getLogger(getClass()).warn("Unable to find variable named " + varName + ". Returning original value of " + var);
            return var;
        }
    }

    private Param getParam(String name, List<Param> params) {
        String[] names = name.split("\\.");
        String paramName = "";
        for (int i = 0; i < names.length; i++) {
            if (i != 0)
                paramName += ".";
            paramName += names[i];
            int paramNumber = -1;
            Matcher m = templateRowMatcherPattern.matcher(paramName);
            if (m.matches()) {
                paramName = m.group(1);
                paramNumber = Integer.parseInt(m.group(2));
            }
            String nameRemainder = null;
            if (names.length - i > 1) {
                int offset = 1;
                if (paramNumber != -1) {
                    offset += new Integer(paramNumber).toString().length() + 2;
                }
                nameRemainder = name.substring(paramName.length() + offset);
            }
            int paramCount = 0;
            for (Param param : params) {
                if (param.getName().equals(paramName)) {
                    if (paramNumber == -1 || ++paramCount == paramNumber) {
                        if (nameRemainder != null) {
                            Param p = getParam(nameRemainder, Arrays.asList(param.getParam()));
                            if (p != null) {
                                return p;
                            }
                        } else {
                            return param;
                        }
                    }
                } else if (param.getName().equals(name)) {
                    return param;
                }
            }
        }
        return null;

    }

    private List<Param> deepCopyParams(Param[] params, Map<String, String> variableMap) {
        List<Param> copiedParams = new ArrayList<Param>(params.length);
        for (Param param : params) {
            Param newParam = new Param();
            newParam.setName(param.getName());
            newParam.setValue(param.getValue());
            doVariableReplacement(newParam, variableMap);
            List<Param> newChildParams = new ArrayList<Param>();
            newParam.setUseTemplate(param.getUseTemplate());
            newChildParams.addAll(deepCopyParams(param.getParam(), variableMap));
            newParam.setParam(newChildParams.toArray(new Param[newChildParams.size()]));
            copiedParams.add(newParam);
        }
        return copiedParams;
    }

}
