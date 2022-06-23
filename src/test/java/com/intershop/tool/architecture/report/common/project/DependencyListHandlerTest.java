package com.intershop.tool.architecture.report.common.project;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DependencyListHandlerTest
{
    private static final String TEST_TXT = "handler-test-dependencies.txt";
    private final DependencyListHandler underTest = new DependencyListHandler();

    @Test
    public void test()
    {
        File file = new File(getClass().getClassLoader().getResource(TEST_TXT).getFile());
        DependencyListHandler handler = underTest.parse(file);
        Collection<ProjectRef> projects = handler.getProjects();

        ProjectRef projectRefSelf = getProjectRef(projects, "ft_icm_as");
        assertNull(projectRefSelf, "found not allowed project reference in dependency list");
        assertNotNull(handler.getProject(), "cannot find project self reference");
        assertEquals("com.intershop.icm:ft_icm_as:LOCAL", handler.getProject().getFullIdentifier(), "found incorrect full identifier of project");

        ProjectRef projectRef = getProjectRef(projects, "guice");
        ProjectRef projectSubmoduleRef = getProjectRef(projects, "bc_dashboard");
        assertEquals(254, projects.size(), "incorrect number of libraries/cartridges");
        assertNotNull(projectRef, "cannot find guice");
        assertEquals("5.0.1", projectRef.getVersion(), "found incorrect version of guice");
        assertNotNull(projectSubmoduleRef, "cannot find bc_dashboard");
        assertEquals("LOCAL", projectSubmoduleRef.getVersion(), "found incorrect version of bc_dashboard");
    }

    private static ProjectRef getProjectRef(Collection<ProjectRef> dependencies, String name)
    {
        Optional<ProjectRef> findResult = dependencies.stream().filter(pr -> name.equals(pr.getName())).findAny();
        return findResult.orElse(null);
    }
}
