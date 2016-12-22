package com.intershop.tool.architecture.report.cmd;

import java.io.File;

public class ArchitectureReportOutputFolder
{
    public static final File DEFAULT_REPORT_DIRECTORY = new File("build/reports/architecture_report");

    private final File reportDirectory;

    public ArchitectureReportOutputFolder()
    {
        this(DEFAULT_REPORT_DIRECTORY);
    }

    public ArchitectureReportOutputFolder(String location)
    {
        this(location == null ? DEFAULT_REPORT_DIRECTORY : new File(location));
    }

    public ArchitectureReportOutputFolder(File reportDirectory)
    {
        this.reportDirectory = reportDirectory == null ? DEFAULT_REPORT_DIRECTORY : reportDirectory;
        if (!reportDirectory.exists())
        {
            reportDirectory.mkdirs();
        }
    }

    public File getNewIssuesFile()
    {
        return new File(reportDirectory, "new_issues.xml");
    }

    public File getFixedIssuesFile()
    {
        return new File(reportDirectory, "fixed_issues.xml");
    }

    public File getApiDefinitionFile()
    {
        return new File(reportDirectory, "api_definition.xml");
    }

    public File getApiViolationFile()
    {
        return new File(reportDirectory, "api_violation.xml");
    }
}
