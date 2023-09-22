package com.intershop.tool.architecture.report.common.issue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.List;

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
     * @param issues List of issues
     */
    public void printIssues(List<Issue> issues)
    {
        issues.sort(IssueComparator.getInstance());
        ArchitectureReportOutputFolder folderLocations = new ArchitectureReportOutputFolder(
                        info.getArgument(ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY));
        File newIssuesFile = folderLocations.getNewIssuesFile();
        try
        {
            try (Formatter formatter = new Formatter(newIssuesFile))
            {
                formatter.format("<azure>\n\t<board>\n");
                for (Issue issue : issues)
                {
                    formatter.format("\t\t<issue project-id=\"%s\" type=\"%s\" work-item-id=\"XXXX\" key=\"%s\">%s</issue>\n",
                                    issue.getProjectRef().getIdentifier(), issue.getKey(), issue.getHash(), issue.getParametersString());
                }
                formatter.format("\t</board>\n</azure>\n");
                formatter.flush();
            }
        }
        catch(FileNotFoundException e)
        {
            LOGGER.error("Can't write errors at " + newIssuesFile.getAbsolutePath(), e);
        }
        LOGGER.error("Architecture report contains new errors, see '{}'.", newIssuesFile.getAbsolutePath());
    }
}
