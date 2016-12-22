package com.intershop.tool.architecture.report.common.messages;

import java.io.Serializable;
import java.util.List;

import com.intershop.tool.architecture.report.common.model.Issue;

public class NewIssuesResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<Issue> issues;

    public NewIssuesResponse(List<Issue> issues)
    {
        this.issues = issues;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }}
