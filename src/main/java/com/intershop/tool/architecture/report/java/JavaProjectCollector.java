package com.intershop.tool.architecture.report.java;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.ProjectProcessor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.Jar;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.jar.JarFinder;

public class JavaProjectCollector implements ProjectProcessor
{
    private JarFinder jarFinder = new JarFinder();
    private final CommandLineArguments info;

    public JavaProjectCollector(CommandLineArguments info)
    {
        this.info = info;
    }

    @Override
    public void process(ProjectRef projectRef, ProjectProcessorResult result)
    {
        File cartridgesDirectory = new File(info.getArgument(ArchitectureReportConstants.ARG_CARTRIDGE_DIRECTORY));
        Collection<File> files = jarFinder.apply(new File(cartridgesDirectory, projectRef.getName() + "/release/lib"));
        JarFileVisitor javaVisitor = new JarFileVisitor(projectRef);
        List<Jar> jars = files.stream().map(f ->  javaVisitor.visitFile(f)).collect(Collectors.toList());
        jars.forEach(jarFile -> process(jarFile, projectRef, result));
    }

    private void process(Jar jar, ProjectRef projectRef, ProjectProcessorResult result)
    {
        List<Definition> definitions = new ArrayList<>();
        jar.getClasses().stream().forEach(jc -> {
            definitions.addAll(jc.getApiDefinition());
        });
        definitions.forEach(d -> d.setProjectRef(projectRef));
        result.definitions.addAll(definitions);
    }

    @Override
    public List<Issue> validate(ProjectRef projectRef, ProjectProcessorResult projectResult)
    {
        return Collections.emptyList();
    }
}