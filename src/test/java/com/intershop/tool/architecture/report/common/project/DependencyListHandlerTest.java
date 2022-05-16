package com.intershop.tool.architecture.report.common.project;

import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        assertNull("found not allowed project reference in dependency list", projectRefSelf);
        assertNotNull("cannot find project self reference", handler.getProject());
        assertEquals("found incorrect full identifier of project", "com.intershop.icm:ft_icm_as:LOCAL", handler.getProject().getFullIdentifier());

        ProjectRef projectRef = getProjectRef(projects, "guice");
        ProjectRef projectSubmoduleRef = getProjectRef(projects, "bc_dashboard");
        assertEquals("incorrect number of libraries/cartridges", 254, projects.size());
        assertNotNull("cannot find guice", projectRef);
        assertEquals("found incorrect version of guice", "5.0.1", projectRef.getVersion());
        assertNotNull("cannot find bc_dashboard", projectSubmoduleRef);
        assertEquals("found incorrect version of bc_dashboard", "LOCAL", projectSubmoduleRef.getVersion());
    }

    private static ProjectRef getProjectRef(Collection<ProjectRef> dependencies, String name)
    {
        Optional<ProjectRef> findResult = dependencies.stream().filter(pr -> name.equals(pr.getName())).findAny();
        return findResult.orElse(null);
    }
}
