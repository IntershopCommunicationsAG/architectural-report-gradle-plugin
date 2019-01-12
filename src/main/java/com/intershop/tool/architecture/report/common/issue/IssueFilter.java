package com.intershop.tool.architecture.report.common.issue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.resources.URILoader;
import com.intershop.tool.architecture.report.common.resources.XMLLoaderException;

public class IssueFilter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueFilter.class);

    private final CommandLineArguments info;

    public IssueFilter(CommandLineArguments info)
    {
        this.info = info;
    }

    public List<Issue> filterIssues(List<Issue> foundIssues)
    {
        String keys = info.getArgument(ArchitectureReportConstants.ARG_KEYS);
        Stream<Issue> stream = foundIssues.stream();
        if (keys != null)
        {
            List<String> keySelector = Arrays.asList(keys.split(","));
            stream = stream.filter(i -> keySelector.contains(i.getKey()));
        }
        Map<String, JiraIssue> existingIssues = getExistingIssues();
        return stream.filter(i -> !existingIssues.containsKey(i.getHash())).collect(Collectors.toList());
    }

    private Map<String, JiraIssue> getExistingIssues()
    {
        String jiraIssueLocation = info.getArgument(ArchitectureReportConstants.ARG_EXISTING_ISSUES_FILE);
        if (jiraIssueLocation == null)
        {
            return Collections.emptyMap();
        }
        Map<String, JiraIssue> result = new HashMap<>();
        try
        {
            InputStream is = URILoader.getInputStream(jiraIssueLocation);
            if (is != null)
            {
                List<JiraIssue> issues = new JiraIssuesVisitor().apply(is);
                issues.stream().forEach(issue -> result.put(issue.getKey(), issue));
            }
        }
        catch(XMLLoaderException|IOException e)
        {
            LOGGER.warn("Can't load issues file: " + jiraIssueLocation, e);
        }
        return result;
    }
}
