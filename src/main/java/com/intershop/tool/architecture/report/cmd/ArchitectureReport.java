package com.intershop.tool.architecture.report.cmd;

import java.io.FileNotFoundException;

import com.intershop.tool.architecture.report.common.issue.IssueCollector;
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
            if (ArchitectureReport.validateArchitecture(args))
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
    public static boolean validateArchitecture(String... args) throws FileNotFoundException
    {
        CommandLineArguments info = new CommandLineArguments(args);
        IssueCollector collector = new ServerCollector(info);
        IssuePrinter printer = new IssuePrinter(info);
        return printer.printIssues(collector.validate());
    }
}
