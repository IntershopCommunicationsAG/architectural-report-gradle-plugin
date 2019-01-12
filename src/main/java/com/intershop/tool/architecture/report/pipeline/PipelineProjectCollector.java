package com.intershop.tool.architecture.report.pipeline;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.ProjectProcessor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.pipeline.model.Pipeline;
import com.intershop.tool.architecture.report.pipeline.model.PipelineFinder;
import com.intershop.tool.architecture.report.pipeline.model.PipelineVisitor;
import com.intershop.tool.architecture.report.pipeline.validation.PipelineProcessor;

public class PipelineProjectCollector implements ProjectProcessor
{
    private final CommandLineArguments info;
    private final ProjectRef projectRef;

    public PipelineProjectCollector(CommandLineArguments info, ProjectRef projectRef)
    {
        this.info = info;
        this.projectRef = projectRef;
    }

    @Override
    public List<Issue> validate(ProjectProcessorResult unused)
    {
        File cartridgesDirectory = new File(info.getArgument(ArchitectureReportConstants.ARG_CARTRIDGE_DIRECTORY));
        PipelineFinder finder = new PipelineFinder();
        Collection<File> files = finder.apply(new File(cartridgesDirectory, projectRef.getName() + "/release/pipelines"));
        Function<File, Pipeline> converter = new PipelineVisitor();
        PipelineProcessor processor = new PipelineProcessor();
        files.forEach(f -> processor.process(projectRef, converter.apply(f)));
        return processor.collectIssues(projectRef);
    }

    @Override
    public void process(ProjectProcessorResult result)
    {
        // no preparation needed
    }
}
