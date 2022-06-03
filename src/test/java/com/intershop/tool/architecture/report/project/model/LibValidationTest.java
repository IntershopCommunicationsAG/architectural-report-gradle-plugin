package com.intershop.tool.architecture.report.project.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.intershop.tool.architecture.report.common.project.DependencyListVisitor;
import com.intershop.tool.architecture.report.common.project.LibDefinitionMapper;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import org.junit.Test;

import com.intershop.tool.architecture.report.api.model.actor.DefinitionComparer;
import com.intershop.tool.architecture.report.api.model.definition.APIDefinition;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.resources.ResourceLoader;
import com.intershop.tool.architecture.report.common.resources.XmlLoader;
import com.intershop.tool.architecture.versions.UpdateStrategy;

public class LibValidationTest
{
    private static final String TEST_DEPENDENCIES = "current-dependencies.txt";
    private static final DependencyListVisitor dependencyListVisitor = new DependencyListVisitor();
    private static final ProjectRef testProjectRef = new ProjectRef("com.intershop.tool.architecture", "report", "LOCAL");
    private static final LibDefinitionMapper DEFINITION_MAPPER = new LibDefinitionMapper(testProjectRef);

    @Test
    public void test() throws IOException
    {
        File file = new File(getClass().getClassLoader().getResource(TEST_DEPENDENCIES).getFile());
        Collection<ProjectRef> projects = dependencyListVisitor.apply(file);
        List<Definition> definitions = projects.stream().map(DEFINITION_MAPPER).collect(Collectors.toList());
        XmlLoader xmlLoader = new XmlLoader();
        try (InputStream is = ResourceLoader.getInputStream("baseline_libs.xml"))
        {
            APIDefinition baselineDefinition = xmlLoader.importXML(is, APIDefinition.class);
            DefinitionComparer issueCollector = new DefinitionComparer(testProjectRef, definitions, baselineDefinition.getDefinition(), UpdateStrategy.MINOR);
            List<Issue> issues = issueCollector.getIssues();
            assertEquals("found problem", 1, issues.size());
            assertEquals("correct error of problem", "com.google.guava:guava update incompatible, was version 16.0", issues.get(0).getParametersString());
        }
    }

}
