package com.intershop.tool.architecture.report.java.validation.capi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.common.model.ValidationResult;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

/**
 * Stateless predicate, identifies that a capi class references an internal class.
 */
public class CapiUsingInternalValidator implements Function<JavaClassRequest, ValidationResult>
{
    /**
     * Identifies capi classes with internal references
     */
    @Override
    public ValidationResult apply(JavaClassRequest javaClassRequest)
    {
        JavaClass javaClass = javaClassRequest.getJavaClass();
        String className = javaClass.getClassName();
        if (!className.contains(".capi."))
        {
            return new ValidationResult(ResultType.FALSE);
        }
        List<String> testClasses = new ArrayList<>(javaClass.getUsageRefs());
        List<Issue> issues = new ArrayList<>();
        for (String javaClassName : testClasses)
        {
            if (javaClassName.contains(".internal."))
            {
                issues.add(new Issue(javaClassRequest.getProjectRef(), "com.intershop.capi.internal", className, javaClassName));
            }
        }
        if (issues.isEmpty())
        {
            return new ValidationResult(ResultType.FALSE);
        }
        return new ValidationResult(ResultType.TRUE, issues);
    }
}
