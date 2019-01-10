package com.intershop.tool.architecture.report.common.issue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.intershop.tool.architecture.report.common.project.ProjectRef;

public class ValidationResult
{
    public static final ValidationResult EMPTY = new ValidationResult(ResultType.TRUE);

    private final ResultType resultType;
    private final List<Issue> issues;

    /**
     * Create a wait result
     * @param resultType result of validation
     */
    public ValidationResult(ResultType resultType)
    {
        this(resultType, Collections.emptyList());
    }
    /**
     * Create a is invalid result with one issue
     *
     * @param projectRef project
     * @param key issue key
     * @param parameters issue parameter
     */
    public ValidationResult(ProjectRef projectRef, String key, Object... parameters)
    {
        this(ResultType.FALSE, Arrays.asList(new Issue(projectRef, key, parameters)));
    }

    /**
     * Create a is invalid result with multiple issues
     *
     * @param resultType result of validation
     * @param issues list of issues
     */
    public ValidationResult(ResultType resultType, List<Issue> issues)
    {
        this.resultType = resultType;
        this.issues = Collections.unmodifiableList(issues);
    }

    /**
     *
     * @return {@link ResultType#TRUE} means is valid
     */
    public ResultType getResultType()
    {
        return resultType;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }
}
