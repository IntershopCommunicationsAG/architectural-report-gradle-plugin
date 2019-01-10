package com.intershop.tool.architecture.report.java.validation.capi;

import java.util.ArrayList;
import java.util.List;

import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;

/**
 * Stateless predicate, identifies that a capi class references an internal class.
 */
public class CapiUsingInternalValidator
{
    /**
     * Identifies capi classes with internal references
     * @param projectRef 
     */
    public List<Issue> validate(ProjectRef projectRef, JavaClass javaClass)
    {
        List<Issue> issues = new ArrayList<>();
        String className = javaClass.getClassName();
        if (className.contains(".capi."))
        {
            List<String> testClasses = new ArrayList<>(javaClass.getUsageRefs());
            for (String javaClassName : testClasses)
            {
                if (javaClassName.contains(".internal."))
                {
                    issues.add(new Issue(projectRef, "com.intershop.capi.internal", className, javaClassName));
                }
            }
        }
        return issues;
    }
}
