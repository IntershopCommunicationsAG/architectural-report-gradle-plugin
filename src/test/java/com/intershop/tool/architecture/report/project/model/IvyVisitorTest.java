package com.intershop.tool.architecture.report.project.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import org.junit.Test;

import com.intershop.tool.architecture.report.common.project.IvyVisitor;
import com.intershop.tool.architecture.report.common.project.ProjectRef;

public class IvyVisitorTest
{
    private static final String TEST_IVY = "ivy.xml";
    private IvyVisitor underTest = new IvyVisitor();

    @Test
    public void test()
    {
        File file = new File(getClass().getClassLoader().getResource(TEST_IVY).getFile());
        Collection<ProjectRef> info = underTest.apply(file);
        assertEquals("number of classes", 343, info.size()); // number of dependency elements
        ProjectRef projectRef = getProjectRef(info, "bc_service");
        assertNotNull("found bc_service", projectRef);
        assertEquals("found correct version of bc_service", "1.0.0.0.20150907144424", projectRef.getVersion());
    }

    private static ProjectRef getProjectRef(Collection<ProjectRef> dependencies, String name)
    {
        Optional<ProjectRef> findResult = dependencies.stream().filter(pr -> name.equals(pr.getName())).findAny();
        return findResult.isPresent() ? findResult.get() : null;
    }

}
