package com.intershop.tool.architecture.report.isml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.ProjectProcessor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.isml.model.IsmlFinder;
import com.intershop.tool.architecture.report.isml.model.IsmlTemplateChecker;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business
 * objects only.
 */
public class IsmlTemplateCollector implements ProjectProcessor
{
    private final CommandLineArguments info;
    private final ProjectRef projectRef;

    public IsmlTemplateCollector(CommandLineArguments info, ProjectRef projectRef)
    {
        this.info = info;
        this.projectRef = projectRef;
    }

    @Override
    public void process(ProjectProcessorResult result)
    {
        // nothing todo
    }

    @Override
    public List<Issue> validate(ProjectProcessorResult projectResults)
    {
        File cartridgesDirectory = new File(info.getArgument(ArchitectureReportConstants.ARG_CARTRIDGE_DIRECTORY));
        Collection<File> files = new IsmlFinder().apply(new File(cartridgesDirectory, projectRef.getName() + "/release/templates"));
        List<Issue> result = new ArrayList<>();
        files.forEach(f -> result.addAll(validate(projectRef, f)));
        return result;
    }

    private static List<Issue> validate(ProjectRef projectRef, File ismlFile)
    {
        IsmlTemplateChecker checker = new IsmlTemplateChecker(projectRef, ismlFile);
        return checker.getIssues();
    }
}
