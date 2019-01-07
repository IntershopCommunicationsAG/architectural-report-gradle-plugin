package com.intershop.tool.architecture.report.common.actors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportOutputFolder;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.messages.PrintFixedIssueRequest;
import com.intershop.tool.architecture.report.common.messages.PrintIssueRequest;
import com.intershop.tool.architecture.report.common.messages.PrintNewIssuesRequest;
import com.intershop.tool.architecture.report.common.messages.PrintResponse;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.common.model.JiraIssue;
import com.intershop.tool.architecture.report.common.model.JiraIssuesVisitor;
import com.intershop.tool.architecture.report.common.model.URILoader;
import com.intershop.tool.architecture.report.common.model.XMLLoaderException;

import akka.actor.AbstractActor;

public class IssuePrinterActor extends AbstractActor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IssuePrinterActor.class);
    private ArchitectureReportOutputFolder folderLocations;

    /**
     * set of issue hash keys
     */
    private static final Set<String> detectedIssues = new HashSet<>();
    private static final Set<Issue> newIssues = new HashSet<>();
    private Map<String, JiraIssue> existingIssues = new HashMap<>();
    private Collection<String> keySelector = new ArrayList<>();

    private static void clear()
    {
        detectedIssues.clear();
        newIssues.clear();
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(CommandLineArguments.class, this::receive)
                        .match(PrintIssueRequest.class, this::receive)
                        .match(PrintFixedIssueRequest.class, this::receive)
                        .match(PrintNewIssuesRequest.class, this::receive)
                        .match(PrintIssueRequest.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            clear();
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    private void receive(CommandLineArguments message) throws IOException
    {
        folderLocations = new ArchitectureReportOutputFolder(message.getArgument(ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY));
        existingIssues = getExistingIssues(message.getArgument(ArchitectureReportConstants.ARG_EXISTING_ISSUES_FILE));
        String keys = message.getArgument(ArchitectureReportConstants.ARG_KEYS);
        keySelector = keys == null ? Collections.emptyList() : Arrays.asList(keys.split(","));
    }

    private void receive(PrintFixedIssueRequest message) throws IOException
    {
        try (Formatter formatter = new Formatter(folderLocations.getFixedIssuesFile()))
        {
            for (JiraIssue existingIssue : existingIssues.values())
            {
                if (!detectedIssues.contains(existingIssue.getKey()))
                {
                    formatter.format("FIXED (%s) : %s\n", existingIssue.getKey(), existingIssue.getJiraID());
                }
            }
            formatter.flush();
        }
        getSender().tell(new PrintResponse(message), getSelf());
    }

    private void receive(PrintIssueRequest message) throws IOException
    {
        for (Issue issue : message.getIssues())
        {
            if (existingIssues.containsKey(issue.getHash()))
            {
                detectedIssues.add(issue.getHash());
            }
            else
            {
                newIssues.add(issue);
            }
        }
        getSender().tell(new PrintResponse(message), getSelf());
    }

    private void receive(PrintNewIssuesRequest message) throws FileNotFoundException
    {
        List<Issue> filteredIssues = keySelector.isEmpty() ? new ArrayList<>(newIssues) : newIssues.stream().filter(i -> keySelector.contains(i.getKey()))
                            .collect(Collectors.toList());
        File newIssuesFile = folderLocations.getNewIssuesFile();
        if (!filteredIssues.isEmpty())
        {
            try (Formatter formatter = new Formatter(newIssuesFile))
            {
                formatter.format("<jira-issues>\n<jira>\n");
                for (Issue issue : filteredIssues)
                {
                    formatter.format(
                                    "<jira-issue project-id=\"%s\" type=\"%s\" jira-id=\"XXXX\" key=\"%s\">%s</jira-issue>\n",
                                    issue.getProjectRef().getIdentifier(), issue.getKey(), issue.getHash(),
                                    issue.getParametersString());
                }
                formatter.format("</jira>\n</jira-issues>\n");
                formatter.flush();
            }
            LOGGER.error("Architecture report contains new errors, see '{}'.", newIssuesFile.getAbsolutePath());
        }
        getSender().tell(new PrintResponse(message, filteredIssues), getSelf());
    }

    private static Map<String, JiraIssue> getExistingIssues(String jiraIssueLocation) throws IOException
    {
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
        catch(XMLLoaderException e)
        {
            LoggerFactory.getLogger(IssuePrinterActor.class).warn("Can't load issues file: " + jiraIssueLocation, e);
        }
        return result;
    }
}
