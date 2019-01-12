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
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.validation.capi.CapiUsingInternalValidator;

public class JavaProjectCollector implements ProjectProcessor
{
    private final CommandLineArguments info;
    private final ProjectRef projectRef;
    private List<Jar> jars = Collections.emptyList();

    public JavaProjectCollector(CommandLineArguments info, ProjectRef projectRef)
    {
        this.info = info;
        this.projectRef = projectRef;
    }

    @Override
    public void process(ProjectProcessorResult result)
    {
        File cartridgesDirectory = new File(info.getArgument(ArchitectureReportConstants.ARG_CARTRIDGE_DIRECTORY));
        Collection<File> files = new JarFinder().apply(new File(cartridgesDirectory, projectRef.getName() + "/release/lib"));
        JarFileVisitor javaVisitor = new JarFileVisitor(projectRef);
        jars = files.stream().map(f ->  javaVisitor.visitFile(f)).collect(Collectors.toList());
        jars.forEach(jarFile -> process(jarFile, result));
    }

    private void process(Jar jar, ProjectProcessorResult result)
    {
        List<Definition> definitions = new ArrayList<>();
        jar.getClasses().stream().forEach(jc -> {
            definitions.addAll(jc.getApiDefinition());
        });
        definitions.forEach(d -> d.setProjectRef(projectRef));
        result.definitions.addAll(definitions);
    }

    @Override
    public List<Issue> validate(ProjectProcessorResult projectResult)
    {
        List<Issue> result = new ArrayList<>(); 
        jars.forEach(jarFile -> result.addAll(process(jarFile, projectRef)));
        return result;
    }

    private Collection<Issue> process(Jar jarFile, ProjectRef projectRef)
    {
        List<Issue> result = new ArrayList<>(); 
        jarFile.getClasses().forEach(jc -> result.addAll(process(jc, projectRef)));
        return result;
    }

    private static CapiUsingInternalValidator capiClassValidator = new CapiUsingInternalValidator();
    private Collection<Issue> process(JavaClass javaClass, ProjectRef projectRef)
    {
        return capiClassValidator.validate(projectRef, javaClass);
    }
}