package com.intershop.tool.architecture.report.cmd;

import java.io.FileNotFoundException;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.issue.IssueCollector;
import com.intershop.tool.architecture.report.common.issue.IssueFilter;
import com.intershop.tool.architecture.report.common.issue.IssuePrinter;
import com.intershop.tool.architecture.report.server.ServerCollector;

public class ArchitectureReport
{

    /**
     * Execute Architecture report on commandline
     * 
     * @param args command line arguments
     * <ul>
     *     <li>server dependencies file:
     *         d:\Source\ish\gradle_trunk\server\share\dependencies.txt</li>
     *     <li>cartridge directory:
     *         d:\Source\ish\gradle_trunk\server\share\system\cartridges</li>
     *     <li>api baseline resource e.g.
     *         api_definition_baseline_7.7.xml</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        try
        {
            if (ArchitectureReport.validateArchitecture(args))
            {
                System.exit(3);
            }
        }
        catch(Exception e)
        {
            LoggerFactory.getLogger(ArchitectureReport.class).error("Architecture report failed.", e);
            System.exit(1);
        }
    }

    /**
     * @param args program arguments
     * @return true in case validation is failing
     */
    public static boolean validateArchitecture(String[] args) throws FileNotFoundException
    {
        CommandLineArguments info = new CommandLineArguments(args);
        IssueCollector collector = new ServerCollector(info);
        List<Issue> allIssues = collector.validate();
        IssueFilter filter = new IssueFilter(info);
        List<Issue> filteredIssues = filter.filterIssues(allIssues);
        if (filteredIssues.isEmpty())
        {
            return false;
        }
        IssuePrinter printer = new IssuePrinter(info);
        printer.printIssues(filteredIssues);
        return true;
    }
}
