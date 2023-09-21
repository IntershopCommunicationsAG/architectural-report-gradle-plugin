package com.intershop.tool.architecture.report.common.issue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IssueLogger
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueLogger.class);

    /**
     * @param issues List of issues
     */
    public void logIssues(List<Issue> issues)
    {
        AtomicInteger idx = new AtomicInteger(1);
        String renderedIssues = issues.stream().sorted(IssueComparator.getInstance()).map(issue ->
            String.format("%d: project=%s, key=%s, parameters=%s", idx.getAndIncrement(), issue.getProjectRef().getIdentifier(), issue.getKey(), issue.getParametersString())
        ).collect(Collectors.joining("\n"));

        LOGGER.error("Architecture report contains the following new errors:\n{}", renderedIssues);
    }
}
