package com.intershop.tool.architecture.report.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.intershop.tool.architecture.report.api.model.actor.LibraryUpdateProcessor;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.issue.IssueCollector;
import com.intershop.tool.architecture.report.common.project.GlobalProcessor;
import com.intershop.tool.architecture.report.common.project.IvyVisitor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.isml.IsmlTemplateCollector;
import com.intershop.tool.architecture.report.java.JavaProjectCollector;
import com.intershop.tool.architecture.report.pipeline.PipelineProjectCollector;

public class ServerCollector implements IssueCollector
{
    private static final IvyVisitor IVY_VISITOR = new IvyVisitor();

    private final CommandLineArguments info;

    public ServerCollector(CommandLineArguments info)
    {
        this.info = info;
    }
    

    private List<GlobalProcessor> getGlobalProcessor()
    {
        List<GlobalProcessor> result = new ArrayList<>();
        result.add(new LibraryUpdateProcessor(info));
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
        List<GlobalProcessor> globalProcessors = getGlobalProcessor();

        ProjectProcessorResult projectResults = new ProjectProcessorResult();
        // process globals
        globalProcessors.forEach(c -> c.process(projectResults));
        Collection<ProjectRef> projects = IVY_VISITOR.apply(new File(info.getArgument(ArchitectureReportConstants.ARG_IVYFILE)));

        List<ProjectProcessor> projectProcessors = new ArrayList<>();
        projects.forEach(p -> {
            projectProcessors.addAll(getProjectProcessor(p));
        });
        // process projects
        projectProcessors.forEach(c -> c.process(projectResults));
        projectProcessors.forEach(c -> result.addAll(c.validate(projectResults)));
        // collect globals
        globalProcessors.forEach(c -> result.addAll(c.validate(projectResults)));
        return result;
    }
}
