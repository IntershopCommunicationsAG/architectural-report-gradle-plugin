package com.intershop.tool.architecture.report.java.validation.bo.identification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.jar.model.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class BusinessObjectClassPredicateTest
{
    private static final String TEST_JAR = "test_ca.jar";
    private BusinessObjectPredicate predicate = new BusinessObjectPredicate();
    private JarFileVisitor jarVisitor = new JarFileVisitor(new ProjectRef("test.group", "test", "1.0"));

    @Test
    public void test() throws IOException
    {
        Collection<JavaClass> jarClasses = jarVisitor.getClasses(TEST_JAR);
        Collection<JavaClass> boClasses = jarClasses.stream().filter(javaClass -> ResultType.TRUE.equals(predicate.apply(javaClass))).collect(Collectors.toList());
        assertFalse("found business objects", boClasses.isEmpty());

        Optional<JavaClass> catalogBOClass = jarClasses.stream().filter(javaClass -> javaClass.getClassName().equals("com.intershop.component.catalog.capi.CatalogBO")).findAny();
        assertTrue("found catalogbo", catalogBOClass.isPresent());

        assertEquals("CatalogCategoryBO is business object", ResultType.TRUE, predicate.apply("com.intershop.component.catalog.capi.CatalogCategoryBO"));
        assertEquals("CatalogBO is business object", ResultType.WAIT, predicate.apply("com.intershop.component.catalog.capi.CatalogBO"));
        assertEquals("CatalogBO is business object", ResultType.TRUE, predicate.apply(catalogBOClass.get()));
        assertEquals("CatalogBO is business object", ResultType.TRUE, predicate.apply("com.intershop.component.catalog.capi.CatalogBO"));
    }

}
