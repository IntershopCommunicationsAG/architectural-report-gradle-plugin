package com.intershop.tool.architecture.report.java.validation.capi;

import java.io.Serializable;
import java.util.List;

import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

public class ValidateCapiResponse implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final JavaClassRequest request;
    private final List<Issue> issues;

    public ValidateCapiResponse(JavaClassRequest request, List<Issue> issues)
    {
        this.request = request;
        this.issues = issues;
    }

    public JavaClassRequest getRequest()
    {
        return request;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }
}