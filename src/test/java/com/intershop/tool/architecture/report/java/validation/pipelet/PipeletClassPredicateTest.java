package com.intershop.tool.architecture.report.java.validation.pipelet;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.junit.Test;

import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.jar.model.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class PipeletClassPredicateTest
{
    private static final String TEST_JAR = "test_co.jar";
    private JarFileVisitor jarVisitor = new JarFileVisitor(new ProjectRef("test.group", "test", "1.0"));

    @Test
    public void testCorePipelets() throws IOException
    {
        Collection<JavaClass> classes = jarVisitor.getClasses(TEST_JAR);
        PipeletClassPredicate under_test = new PipeletClassPredicate();
        classes.stream().forEach(under_test::apply);
        assertEquals("GroupObjectsByRanges is pipelet", ResultType.TRUE, under_test.apply("com.intershop.beehive.core.pipelet.GroupObjectsByRanges"));

        assertEquals("PipelineConstants inside of pipelets package is no pipelet", ResultType.FALSE, under_test.apply("com.intershop.beehive.core.pipelet.PipelineConstants"));

        assertEquals("GroupObjectsByRanges is pipelet", ResultType.TRUE, under_test.apply(getClass(classes, "com.intershop.beehive.core.pipelet.GroupObjectsByRanges")).getResultType());

        assertEquals("count pipelets", 126, classes.stream().filter(t -> ResultType.TRUE.equals(under_test.apply(t).getResultType())).count());
    }

    private static JavaClass getClass(Collection<JavaClass> classes, String className)
    {
        Optional<JavaClass> findFirst = classes.stream().filter(c -> c.getClassName().equals(className)).findFirst();
        return findFirst.isPresent() ? findFirst.get() : null;
    }
}
