package com.intershop.tool.architecture.report.server;

import com.intershop.tool.architecture.report.api.model.actor.LibraryUpdateProcessor;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.issue.IssueCollector;
import com.intershop.tool.architecture.report.common.project.*;
import com.intershop.tool.architecture.report.isml.IsmlTemplateCollector;
import com.intershop.tool.architecture.report.java.JavaApplicationProcessor;
import com.intershop.tool.architecture.report.java.JavaProjectCollector;
import com.intershop.tool.architecture.report.pipeline.PipelineProjectCollector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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

    private List<ProjectProcessor> getProjectProcessor(ProjectRef projectRef)
    {
        List<ProjectProcessor> result = new ArrayList<>();
        result.add(new IsmlTemplateCollector(info, projectRef));
        result.add(new PipelineProjectCollector(info, projectRef));
        result.add(new JavaProjectCollector(info, projectRef));
        return result;
    }

    public List<Issue> validate()
    {
        List<Issue> result = new ArrayList<>();
        List<GlobalProcessor> globalProcessors = getGlobalProcessors();

        ProjectProcessorResult projectResults = new ProjectProcessorResult();
        // process globals
        globalProcessors.forEach(c -> c.process(projectResults));
        
        if (info.getArgument(ArchitectureReportConstants.ARG_DEPENDENCIES_FILE) != null)
        {
            File dependenciesFile = new File(info.getArgument(ArchitectureReportConstants.ARG_DEPENDENCIES_FILE));
            Collection<ProjectRef> projects = new DependencyListVisitor().apply(dependenciesFile);
            List<ProjectProcessor> projectProcessors = new ArrayList<>();
            projects.forEach(p -> projectProcessors.addAll(getProjectProcessor(p)));
            // process projects
            projectProcessors.forEach(c -> c.process(projectResults));
            projectProcessors.forEach(c -> result.addAll(c.validate(projectResults)));
        }
        // collect globals
        globalProcessors.forEach(c -> result.addAll(c.validate(projectResults)));
        return result;
    }
}
