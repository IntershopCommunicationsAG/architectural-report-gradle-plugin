package com.intershop.tool.architecture.report.cmd;

import java.io.FileNotFoundException;
import java.util.List;

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
     * @param args
     *            command line arguments
     *            <ul>
     *            <li>server ivy file:
     *            d:\Source\ish\gradle_trunk\server\share\ivy.xml</li>
     *            <li>cartridge directory:
     *            d:\Source\ish\gradle_trunk\server\share\system\cartridges</li>
     *            <li>api baseline resource e.g.
     *            api_definition_baseline_7.7.xml</li>
     *            </ul>
     */
    public static void main(String[] args)
    {
        try
        {
            CommandLineArguments info = new CommandLineArguments(args);
            if (ArchitectureReport.validateArchitecture(info))
            {
                System.exit(3);
            }
        }
        catch(Exception e)
        {
            System.exit(1);
        }
    }

    /**
     * @param args
     *            program arguments
     * @return true in case validation is failing
     * @throws FileNotFoundException
     */
    public static boolean validateArchitecture(CommandLineArguments args) throws FileNotFoundException
    {
        IssueCollector collector = new ServerCollector(args);
        List<Issue> allIssues = collector.validate();
        IssueFilter filter = new IssueFilter(args);
        List<Issue> filteredIssues = filter.filterIssues(allIssues);
        if (filteredIssues.isEmpty())
        {
            return false;
        }
        IssuePrinter printer = new IssuePrinter(args);
        printer.printIssues(filteredIssues);
        return true;
    }
}
