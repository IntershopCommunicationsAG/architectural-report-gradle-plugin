package com.intershop.tool.architecture.report.common.issue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportOutputFolder;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;

public class IssuePrinter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IssuePrinter.class);
    private final CommandLineArguments info;

    public IssuePrinter(CommandLineArguments info)
    {
        this.info = info;
    }

    /**
     * @param allIssues
     * @return true in case issues are printed
     */
    public boolean printIssues(List<Issue> allIssues)
    {
        return printFilteredIssues(filterIssues(allIssues));
    }

    private List<Issue> filterIssues(List<Issue> foundIssues)
    {
        String keys = info.getArgument(ArchitectureReportConstants.ARG_KEYS);
        if (keys == null)
        {
            return foundIssues;
        }
        List<String> keySelector = Arrays.asList(keys.split(","));
        return foundIssues.stream().filter(i -> keySelector.contains(i.getKey())).collect(Collectors.toList());
    }

    private boolean printFilteredIssues(List<Issue> filteredIssues)
    {
        if (filteredIssues.isEmpty())
        {
            return true;
        }
        ArchitectureReportOutputFolder folderLocations = new ArchitectureReportOutputFolder(
                        info.getArgument(ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY));
        File newIssuesFile = folderLocations.getNewIssuesFile();
        try
        {
            try (Formatter formatter = new Formatter(newIssuesFile))
            {
                formatter.format("<jira-issues>\n<jira>\n");
                for (Issue issue : filteredIssues)
                {
                    formatter.format("<jira-issue project-id=\"%s\" type=\"%s\" jira-id=\"XXXX\" key=\"%s\">%s</jira-issue>\n",
                                    issue.getProjectRef().getIdentifier(), issue.getKey(), issue.getHash(), issue.getParametersString());
                }
                formatter.format("</jira>\n</jira-issues>\n");
                formatter.flush();
            }
        }
        catch(FileNotFoundException e)
        {
            LOGGER.error("Can't write errors at " + newIssuesFile.getAbsolutePath(), e);
        }
        LOGGER.error("Architecture report contains new errors, see '{}'.", newIssuesFile.getAbsolutePath());
        return false;
    }
}
