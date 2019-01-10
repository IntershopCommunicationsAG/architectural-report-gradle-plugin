package com.intershop.tool.architecture.report.common.project;

import java.util.List;

import com.intershop.tool.architecture.report.common.issue.Issue;

public interface GlobalProcessor
{
    void process(ProjectProcessorResult projectResult);
    List<Issue> validate(ProjectProcessorResult projectResult);
}
