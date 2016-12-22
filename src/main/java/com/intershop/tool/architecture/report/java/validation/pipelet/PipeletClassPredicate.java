package com.intershop.tool.architecture.report.java.validation.pipelet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.java.model.JavaHelper;
import com.intershop.tool.architecture.report.java.model.WaitForJavaClassResult;

/**
 * Stateful predicate, which returns three results: YES, NO and WAIT.
 * <ul>
 * <li>YES - class can be identified as persistent class</li>
 * <li>NO - class doesn't depend on persistent classes</li>
 * <li>WAIT - the predicate as not enough information about depending classes</li>
 *</ul>
 *
 * A persistent class is a class, which depends on
 */
public class PipeletClassPredicate implements Function<String, ResultType>
{
    /**
     * predefined persistent classes outside of "com.intershop.beehive.orm"
     */
    private static final List<String> pClasses = Arrays.asList("com.intershop.beehive.core.capi.pipeline.Pipelet", "com.intershop.beehive.emf.pipelet.Pipelet");

    /**
     * contains all known persistent classes outside of
     * "com.intershop.beehive.orm"
     */
    private final ConcurrentHashMap<String, String> pipeletClasses = new ConcurrentHashMap<String, String>();
    {
        for (String className : pClasses)
        {
            pipeletClasses.put(className, className);
        }
    }

    /**
     * contains all known non persistent classes
     */
    private final ConcurrentHashMap<String, String> transientClasses = new ConcurrentHashMap<String, String>();
    {
        transientClasses.put("java.lang.Object", "java.lang.Object");
    }

    /**
     * @param javaClass validates this class
     * @return result for validation
     */
    public WaitForJavaClassResult apply(JavaClass javaClass)
    {
        String testClassName = javaClass.getClassName();
        ResultType result = apply(testClassName);
        if (!ResultType.WAIT.equals(result))
        {
            // directly found
            return new WaitForJavaClassResult(result, testClassName);
        }
        // relevant dependent classes are extends X and implements Y (usage
        // inside is not relevant)
        Set<String> testClassNames = new HashSet<String>(javaClass.getImplementsRef());
        testClassNames.add(javaClass.getSuperName());
        String waitFor = null;
        for (String className : testClassNames)
        {
            if (!className.equals(testClassName))
            {
                ResultType tempResult = apply(className);
                if (ResultType.TRUE.equals(tempResult))
                {
                    pipeletClasses.put(testClassName, className);
                    return new WaitForJavaClassResult(ResultType.TRUE, className);
                }
                else if (ResultType.WAIT.equals(tempResult))
                {
                    waitFor = className;
                }
            }
        }
        if (waitFor != null)
        {
            // System.out.println("MISS: " + waitFor + " for " + testClassName);
            return new WaitForJavaClassResult(ResultType.WAIT, waitFor);
        }
        transientClasses.put(testClassName, testClassName);
        return new WaitForJavaClassResult(ResultType.FALSE, testClassName);
    }

    /**
     * @param className java class to test
     * @return current information about class
     */
    @Override
    public ResultType apply(String className)
    {
        if (pipeletClasses.containsKey(className))
        {
            return ResultType.TRUE;
        }
        if (transientClasses.containsKey(className) || JavaHelper.isStandardClass(className))
        {
            return ResultType.FALSE;
        }
        return ResultType.WAIT;
    }
}
