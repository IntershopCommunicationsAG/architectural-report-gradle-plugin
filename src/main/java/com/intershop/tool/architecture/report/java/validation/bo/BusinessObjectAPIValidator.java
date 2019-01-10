package com.intershop.tool.architecture.report.java.validation.bo;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.common.issue.ResultType;
import com.intershop.tool.architecture.report.common.issue.ValidationResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;

/**
 * Validates a business object API java class
 */
public class BusinessObjectAPIValidator
{
    private final Function<String, ResultType> persistentPredicate;
    private final Function<JavaClass, ResultType> businessObjectPredicate;

    public BusinessObjectAPIValidator(Function<String, ResultType> persistentPredicate,
                    Function<JavaClass, ResultType> businessObjectPredicate)
    {
        this.persistentPredicate = persistentPredicate;
        this.businessObjectPredicate = businessObjectPredicate;
    }

    /**
     * @return {@link ResultType#TRUE} if class is valid (means no business
     *         object, internal or clean capi business object) <br>
     *         {@link ResultType#FALSE} if class is dirty capi business object <br>
     *         {@link ResultType#WAIT} if class can't be analyst, because
     *         dependent classes are not resolved yet
     *
     */
    public ValidationResult validate(ProjectRef projectRef, JavaClass javaClass)
    {
        String className = javaClass.getClassName();
        if (!className.contains(".capi."))
        {
            // it's fine it not a business object or outside of capi (not API)
            return new ValidationResult(ResultType.TRUE);
        }
        ResultType isBusinessObject = businessObjectPredicate.apply(javaClass);
        if (ResultType.FALSE.equals(isBusinessObject))
        {
            return new ValidationResult(ResultType.TRUE);
        }
        if (ResultType.WAIT.equals(isBusinessObject))
        {
            return new ValidationResult(ResultType.WAIT);
        }

        Set<String> testClasses = new HashSet<>();
        testClasses.addAll(javaClass.getUsageRefs());
        testClasses.addAll(javaClass.getImplementsRef());

        for (String javaClassName : testClasses)
        {
            if (javaClassName.contains(".internal."))
            {
                return new ValidationResult(projectRef, ArchitectureReportConstants.KEY_BO_INTERNAL, className, javaClassName);
            }
            ResultType resultPersistence = persistentPredicate.apply(javaClassName);
            if (ResultType.TRUE.equals(resultPersistence))
            {
                return new ValidationResult(projectRef, ArchitectureReportConstants.KEY_BO_PERSISTENCE, className, javaClassName);
            }
            if (ResultType.WAIT.equals(resultPersistence))
            {
                return new ValidationResult(ResultType.WAIT);
            }
        }
        return new ValidationResult(ResultType.TRUE);
    }

}
