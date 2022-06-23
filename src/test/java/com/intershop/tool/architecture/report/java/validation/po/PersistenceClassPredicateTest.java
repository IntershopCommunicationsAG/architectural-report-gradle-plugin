package com.intershop.tool.architecture.report.java.validation.po;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.intershop.tool.architecture.report.common.issue.ResultType;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.model.jclass.WaitForJavaClassResult;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        // assertTrue(classes.isEmpty(), "all classes are processed");
        assertEquals(ResultType.WAIT, predicate.apply("com.intershop.beehive.core.capi.cache.PersistentObjectCacheClearKeyProvider"), "cache cant be resolved");
        assertEquals(ResultType.TRUE, predicate.apply("com.intershop.beehive.core.capi.domain.AttributeValuePOKey"), "primary key is persistence");
        assertEquals(ResultType.FALSE, predicate.apply("java.io.File"), "file is not persistent");
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
