package org.mdf.mockdata.capture;

import java.lang.reflect.Method;

import org.mdf.mockdata.ReflectionParamBuilderUtil3;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.mdf.mockdata.generated.Test;

public class CaptureUtils {

    public static Test createTest(Method method, Object[] args, Object result) throws Exception {
        Test test = new Test();
        Request tdRequest = new Request();
        Response tdResponse = new Response();
        test.setRequest(tdRequest);
        test.setResponse(tdResponse);
        Param interfaceParam = new Param();
        interfaceParam.setName("serviceInterface");
        interfaceParam.setValue(method.getDeclaringClass().getName());
        test.getRequest().addParam(interfaceParam);
        Param methodNameParam = new Param();
        methodNameParam.setName("methodName");
        methodNameParam.setValue(method.getName());
        test.getRequest().addParam(methodNameParam);
        if (args != null) {
            int argCount = 1;
            for (Object arg : args) {
                Param p = new Param();
                p.setName("arg" + argCount++);
                test.getRequest().addParam(p);
                ReflectionParamBuilderUtil3.buildParamFromObject(arg, p, false);
            }
        }
        if (result != null) {
            Param p = new Param();
            p.setName(result.getClass().getName());
            test.getResponse().addParam(p);
            if (!ReflectionParamBuilderUtil3.isPrimitiveType(method.getReturnType())
                    && !method.getReturnType().isPrimitive()) {
                Param implTypeParam = new Param();
                implTypeParam.setName(ReflectionParamBuilderUtil3.IMPLEMENTATION_TYPE_PARAM_NAME);
                implTypeParam.setValue(result.getClass().getName());
                p.addParam(implTypeParam);
            }
            ReflectionParamBuilderUtil3.buildParamFromObject(result, p, false);
        }
        for (Param p : test.getRequest().getParam()) {
            checkParam(p);
        }
        for (Param p : test.getResponse().getParam()) {
            checkParam(p);
        }
        return test;
    }

    public static void checkParam(Param p) {
        for (Param child : p.getParam()) {
            checkParam(child);
        }
    }
}
