package com.intershop.tool.architecture.report.server;

import com.intershop.tool.architecture.report.api.model.actor.LibraryUpdateProcessor;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.issue.IssueCollector;
import com.intershop.tool.architecture.report.common.project.GlobalProcessor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.java.JavaApplicationProcessor;

import java.util.ArrayList;
import java.util.List;

public class ServerCollector implements IssueCollector
{
    private final CommandLineArguments info;

    public ServerCollector(CommandLineArguments info)
    {
        this.info = info;
    }

    private List<GlobalProcessor> getGlobalProcessors()
    {
        List<GlobalProcessor> result = new ArrayList<>();
        result.add(new LibraryUpdateProcessor(info));
        result.add(new JavaApplicationProcessor(info));
        return result;
    }

    public List<Issue> validate()
    {
        List<Issue> result = new ArrayList<>();
        List<GlobalProcessor> globalProcessors = getGlobalProcessors();

        ProjectProcessorResult projectResults = new ProjectProcessorResult();
        // process globals
        globalProcessors.forEach(c -> c.process(projectResults));
        // collect globals
        globalProcessors.forEach(c -> result.addAll(c.validate(projectResults)));
        return result;
    }
}
