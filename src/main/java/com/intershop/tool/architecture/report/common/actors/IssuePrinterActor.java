package com.intershop.tool.architecture.report.common.actors;

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

import akka.actor.UntypedActor;

public class IssuePrinterActor extends UntypedActor
{
    private ArchitectureReportOutputFolder folderLocations;
    /**
     * set of issue hash keys
     */
    private static final Set<String> detectedIssues = new HashSet<>();
    private static final Set<Issue> newIssues = new HashSet<>();
    private Map<String, JiraIssue> hashToIssueMap = new HashMap<>();
    private Collection<String> keySelector = new ArrayList<>();

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof CommandLineArguments)
        {
            receive((CommandLineArguments) message);
        }
        else if (message instanceof PrintIssueRequest)
        {
            receive((PrintIssueRequest)message);
        }
        else if (message instanceof PrintFixedIssueRequest)
        {
            receive((PrintFixedIssueRequest)message);
        }
        else if (message instanceof PrintNewIssuesRequest)
        {
            receive((PrintNewIssuesRequest)message);
        }
        else if (AkkaMessage.TERMINATE.FLUSH_REQUEST.equals(message))
        {
            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
        }
        else
        {
            unhandled(message);
        }
    }

    private void receive(CommandLineArguments message) throws IOException
    {
        folderLocations = new ArchitectureReportOutputFolder(message.getArgument(ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY));
        hashToIssueMap = getExistingIssues(message.getArgument(ArchitectureReportConstants.ARG_EXISTING_ISSUES_FILE));
        keySelector = Arrays.asList(message.getArgument(ArchitectureReportConstants.ARG_KEYS).split(","));
    }

    private void receive(PrintFixedIssueRequest message) throws IOException
    {
        try (Formatter formatter = new Formatter(folderLocations.getFixedIssuesFile()))
        {
            for (JiraIssue existingIssue : hashToIssueMap.values())
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
            if (hashToIssueMap.containsKey(issue.getHash()))
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
        if (!keySelector.isEmpty())
        {
            List<Issue> filteredIssues = newIssues.stream().filter(i -> keySelector.contains(i.getKey()))
                            .collect(Collectors.toList());
            try (Formatter formatter = new Formatter(folderLocations.getNewIssuesFile()))
            {
                for (Issue issue : filteredIssues)
                {
                    formatter.format(
                                    "<jira-issue project-id=\"%s\" type=\"%s\" jira-id=\"XXXX\" key=\"%s\">%s</jira-issue>\n",
                                    issue.getProjectRef().getIdentifier(), issue.getKey(), issue.getHash(),
                                    issue.getParametersString());
                }
                formatter.flush();
            }
            getSender().tell(new PrintResponse(message, filteredIssues), getSelf());
        }
        else
        {
            getSender().tell(new PrintResponse(message), getSelf());
        }
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
        catch(FileNotFoundException e)
        {
            LoggerFactory.getLogger(IssuePrinterActor.class).warn("Can't load issues file", e);
        }
        return result;
    }
}
