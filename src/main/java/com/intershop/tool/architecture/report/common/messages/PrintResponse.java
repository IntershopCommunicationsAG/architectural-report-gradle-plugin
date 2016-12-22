package com.intershop.tool.architecture.report.common.messages;

import java.util.Collections;
import java.util.List;

import com.intershop.tool.architecture.report.common.model.Issue;

public class PrintResponse
{
    private final PrintRequest request;
    private final List<Issue> issues;

    public PrintResponse(PrintRequest request)
    {
        this(request, Collections.emptyList());
    }

    public PrintResponse(PrintRequest request, List<Issue> issues)
    {
        this.request = request;
        this.issues = issues;
    }

    public PrintRequest getRequest()
    {
        return request;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }

}
