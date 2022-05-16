package com.intershop.tool.architecture.report.java;

import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.GlobalProcessor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.Jar;
import com.intershop.tool.architecture.report.java.model.jar.JarFileListVisitor;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.validation.capi.CapiUsingInternalValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JavaApplicationProcessor implements GlobalProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(JavaApplicationProcessor.class);
    private final CommandLineArguments info;
    private final ProjectRef serverProject;
    private List<Jar> jars = Collections.emptyList();

    public JavaApplicationProcessor(CommandLineArguments info)
    {
        this.info = info;
        this.serverProject = new ProjectRef(
                        info.getArgument(ArchitectureReportConstants.ARG_GROUP),
                        info.getArgument(ArchitectureReportConstants.ARG_ARTIFACT),
                        info.getArgument(ArchitectureReportConstants.ARG_VERSION));
    }

    @Override
    public void process(ProjectProcessorResult result)
    {
        if (info.getArgument(ArchitectureReportConstants.ARG_CLASSPATH_FILES_LIST_FILE) == null ||
            info.getArgument(ArchitectureReportConstants.ARG_CLASSPATH_FILES_LIST_FILE).isEmpty()) {
            return;
        }
        logger.info("Classpath files list: {}", info.getArgument(ArchitectureReportConstants.ARG_CLASSPATH_FILES_LIST_FILE));
        File listFile = new File(info.getArgument(ArchitectureReportConstants.ARG_CLASSPATH_FILES_LIST_FILE));
        // Only return collection of JAR files from list
        Collection<File> jarFiles = new JarFileListVisitor().apply(listFile);

        JarFileVisitor javaVisitor = new JarFileVisitor(serverProject);
        jars = jarFiles.parallelStream().map(javaVisitor::visitFile).collect(Collectors.toList());
        jars.forEach(jarFile -> process(jarFile, result));
    }

    private void process(Jar jar, ProjectProcessorResult result)
    {
        List<Definition> definitions = new ArrayList<>();
        jar.getClasses().forEach(jc -> definitions.addAll(jc.getApiDefinition()));
        definitions.forEach(d -> d.setProjectRef(serverProject));
        result.definitions.addAll(definitions);
    }

    @Override
    public List<Issue> validate(ProjectProcessorResult projectResult)
    {
        List<Issue> result = new ArrayList<>(); 
        jars.forEach(jarFile -> result.addAll(process(jarFile, serverProject)));
        return result;
    }

    private Collection<Issue> process(Jar jarFile, ProjectRef projectRef)
    {
        List<Issue> result = new ArrayList<>(); 
        jarFile.getClasses().forEach(jc -> result.addAll(process(jc, projectRef)));
        return result;
    }

    private static final CapiUsingInternalValidator capiClassValidator = new CapiUsingInternalValidator();
    private Collection<Issue> process(JavaClass javaClass, ProjectRef projectRef)
    {
        return capiClassValidator.validate(projectRef, javaClass);
    }
}