package com.intershop.tool.architecture.report.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.intershop.tool.architecture.report.common.project.DependencyListVisitor;
import com.intershop.tool.architecture.report.common.project.LibDefinitionMapper;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.intershop.tool.architecture.report.api.model.actor.DefinitionComparer;
import com.intershop.tool.architecture.report.api.model.definition.APIDefinition;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.resources.ResourceLoader;
import com.intershop.tool.architecture.report.common.resources.XmlLoader;
import com.intershop.tool.architecture.versions.UpdateStrategy;

public class LibraryValidationTest
{
    private static final ProjectRef testProjectRef = new ProjectRef("com.intershop.tool.architecture", "report", "LOCAL");
    private static final DependencyListVisitor dependencyListVisitor = new DependencyListVisitor();
    private static final LibDefinitionMapper definitionMapper = new LibDefinitionMapper(testProjectRef);
    private static InputStream baselineFileInputStream;
    private static List<Definition> baselineDefinitions;

    @BeforeAll
    public static void setUp() throws IOException
    {
        XmlLoader xmlLoader = new XmlLoader();
        baselineFileInputStream = ResourceLoader.getInputStream("baseline-libs.xml");
        baselineDefinitions = xmlLoader.importXML(baselineFileInputStream, APIDefinition.class).getDefinition();
    }

    @AfterAll
    public static void tearDown() throws IOException
    {
        baselineFileInputStream.close();
    }

    @Test
    public void testMajorAndMinorLibraryUpdate() throws IOException
    {
        File dependenciesFileUnderTest = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("dependencies-major-library-update.txt")).getFile());
        Collection<ProjectRef> projects = dependencyListVisitor.apply(dependenciesFileUnderTest);
        List<Definition> libraryDefinitions = projects.stream().map(definitionMapper).collect(Collectors.toList());

        DefinitionComparer issueCollectorMinor = new DefinitionComparer(testProjectRef, libraryDefinitions, baselineDefinitions, UpdateStrategy.MINOR);
        List<Issue> issuesMajorLibraryUpdate = issueCollectorMinor.getIssues();
        assertEquals(1, issuesMajorLibraryUpdate.size(), "Test of major library update expects only one issue");
        assertEquals("com.google.guava:guava update incompatible, was version 16.0", issuesMajorLibraryUpdate.get(0).getParametersString(), "Test of major library update found unexpected issue message");

        DefinitionComparer issueCollectorMajor = new DefinitionComparer(testProjectRef, libraryDefinitions, baselineDefinitions, UpdateStrategy.MAJOR);
        List<Issue> issuesMinorLibraryUpdate = issueCollectorMajor.getIssues();
        assertEquals(0, issuesMinorLibraryUpdate.size(), "Test of minor library update expects no issues");
    }

    @Test
    public void testNewExternalAndInternalLibraries() throws IOException
    {
        File dependenciesFileUnderTest = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("dependencies-new-libraries.txt")).getFile());
        Collection<ProjectRef> projects = dependencyListVisitor.apply(dependenciesFileUnderTest);
        List<Definition> libraryDefinitions = projects.stream().map(definitionMapper).collect(Collectors.toList());

        DefinitionComparer issueCollector = new DefinitionComparer(testProjectRef, libraryDefinitions, baselineDefinitions, UpdateStrategy.MINOR);
        List<Issue> issues = issueCollector.getIssues();
        assertNotEquals(2, issues.size(), "Test of new libraries cannot contain internal libraries as issue");
        assertEquals("org.springframework:spring-test", issues.get(0).getParametersString(), "Test of new libraries did not find new library spring-test");
        assertEquals(1, issues.size(), "Test of new libraries expects only one issue");
    }
}
