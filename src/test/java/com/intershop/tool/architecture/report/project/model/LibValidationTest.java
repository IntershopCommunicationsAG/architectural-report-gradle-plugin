package com.intershop.tool.architecture.report.project.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.intershop.tool.architecture.report.api.model.actor.DefinitionCollectorIssueCollector;
import com.intershop.tool.architecture.report.api.model.definition.APIDefinition;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.common.model.ResourceLoader;
import com.intershop.tool.architecture.report.common.model.XmlLoader;
import com.intershop.tool.architecture.versions.UpdateStrategy;

public class LibValidationTest
{
    private static final String TEST_IVY = "ivy_currentlibs.xml";
    private static final IvyVisitor ivyVisitor = new IvyVisitor();
    private static final LibDefinitionMapper DEFINITION_MAPPER = new LibDefinitionMapper();

    @Test
    public void test() throws IOException
    {
        File file = new File(getClass().getClassLoader().getResource(TEST_IVY).getFile());
        Collection<ProjectRef> projects = ivyVisitor.apply(file);
        List<Definition> definitions = projects.stream().map(DEFINITION_MAPPER).collect(Collectors.toList());
        XmlLoader xmlLoader = new XmlLoader();
        try (InputStream is = ResourceLoader.getInputStream("baseline_libs.xml"))
        {
            APIDefinition baselineDefinition = xmlLoader.importXML(is, APIDefinition.class);
            DefinitionCollectorIssueCollector issueCollector = new DefinitionCollectorIssueCollector(definitions, baselineDefinition.getDefinition(), UpdateStrategy.MINOR);
            List<Issue> issues = issueCollector.getIssues();
            assertEquals("found problem", 1, issues.size());
            assertEquals("correct error of problem", "com.google.guava:guava=17.0 but was 16.0", issues.get(0).getParametersString());
        }
    }

}
