package com.intershop.tool.architecture.report.java.validation.po;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.intershop.tool.architecture.report.common.issue.ResultType;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.model.jclass.WaitForJavaClassResult;
import com.intershop.tool.architecture.report.java.validation.po.PersistenceClassPredicate;

public class PersistenceClassPredicateTest
{
    private static final String TEST_JAR = "test_co.jar";
    private PersistenceClassPredicate predicate = new PersistenceClassPredicate();
    private JarFileVisitor jarVisitor = new JarFileVisitor(new ProjectRef("test.group", "test", "1.0"));

    @Test
    public void test() throws IOException
    {
        Collection<JavaClass> allClasses = jarVisitor.getClasses(TEST_JAR);
        int size = allClasses.size();
        Collection<JavaClass> classes = allClasses;
        do
        {
            size = classes.size();
            classes = processClasses(classes);
        }
        while(size > classes.size() && !classes.isEmpty());

        // some classes depending on unknown classes (e.g. cache), we can assume, that these classes are not persistent
        // assertTrue("all classes are processed", classes.isEmpty());
        assertEquals("cache cant be resolved", ResultType.WAIT,
                        predicate.apply("com.intershop.beehive.core.capi.cache.PersistentObjectCacheClearKeyProvider"));
        assertEquals("primary key is persistence", ResultType.TRUE, predicate.apply("com.intershop.beehive.core.capi.domain.AttributeValuePOKey"));
        assertEquals("file is not persistent", ResultType.FALSE, predicate.apply("java.io.File"));
    }

    private Collection<JavaClass> processClasses(Collection<JavaClass> classes)
    {
        ArrayList<JavaClass> again = new ArrayList<JavaClass>();
        for (JavaClass javaClass : classes)
        {
            WaitForJavaClassResult isPersistentResult = predicate.apply(javaClass);
            ResultType isPersistent = isPersistentResult.getResultType();
            if (ResultType.WAIT.equals(isPersistent))
            {
                again.add(javaClass);
            }
        }
        return again;
    }
}
