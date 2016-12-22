package com.intershop.tool.architecture.report.isml.messages;

import java.util.List;

import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.common.model.Issue;

public class IsmlValidationResponse
{
    private final FileRequest request;
    private final List<Issue> issues;

    public IsmlValidationResponse(FileRequest request, List<Issue> issues)
    {
        this.request = request;
        this.issues = issues;
    }

    public FileRequest getRequest()
    {
        return request;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }

}
