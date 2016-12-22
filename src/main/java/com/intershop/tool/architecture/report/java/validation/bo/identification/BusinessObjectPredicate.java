package com.intershop.tool.architecture.report.java.validation.bo.identification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.java.model.JavaHelper;

/**
 * Stateful predicate, identifies a business object (objects and repositories)
 * (doesn't matter if API or not)
 */
public class BusinessObjectPredicate implements Function<JavaClass, ResultType>
{
    /**
     * predefined business object classes
     */
    private static final List<String> pClasses = Arrays.asList(
                    "com.intershop.beehive.businessobject.capi.BusinessObject",
                    "com.intershop.beehive.businessobject.capi.BusinessObjectRepository");

    /**
     * contains all known business object classes
     */
    private final ConcurrentHashMap<String, ResultType> boClasses = new ConcurrentHashMap<String, ResultType>();
    {
        for (String className : pClasses)
        {
            boClasses.put(className, ResultType.TRUE);
        }
    }

    /**
     * Stateful predicate, which returns three results: YES, NO and WAIT.
     * Identifies a business object interface (objects and repositories)
     */
    @Override
    public ResultType apply(JavaClass javaClass)
    {
        String className = javaClass.getClassName();
        ResultType tempResult = apply(className);
        if (!ResultType.WAIT.equals(tempResult))
        {
            return tempResult;
        }
        List<String> testClasses = new ArrayList<>(javaClass.getImplementsRef());
        testClasses.add(javaClass.getSuperName());
        for (String javaClassName : testClasses)
        {
            // ignore self containments
            if (!javaClassName.equals(className))
            {
                ResultType result = apply(javaClassName);
                if (ResultType.TRUE.equals(result))
                {
                    boClasses.put(className, ResultType.TRUE);
                    return ResultType.TRUE;
                }
                if (ResultType.WAIT.equals(result))
                {
                    return ResultType.WAIT;
                }
            }
        }
        boClasses.put(className, ResultType.FALSE);
        return ResultType.FALSE;
    }

    /* package */ResultType apply(String className)
    {
        if (JavaHelper.isStandardClass(className))
        {
            return ResultType.FALSE;
        }
        ResultType result = boClasses.get(className);
        return result == null ? ResultType.WAIT : result;
    }
}
