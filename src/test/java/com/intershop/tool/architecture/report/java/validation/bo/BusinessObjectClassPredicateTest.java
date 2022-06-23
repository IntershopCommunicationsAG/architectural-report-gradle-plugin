package com.intershop.tool.architecture.report.java.validation.bo;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.intershop.tool.architecture.report.common.issue.ResultType;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BusinessObjectClassPredicateTest
{
    private static final String TEST_JAR = "test_ca.jar";
    private final BusinessObjectPredicate predicate = new BusinessObjectPredicate();
    private final JarFileVisitor jarVisitor = new JarFileVisitor(new ProjectRef("test.group", "test", "1.0"));

    @Test
    public void test() throws IOException
    {
        Collection<JavaClass> jarClasses = jarVisitor.getClasses(TEST_JAR);
        Collection<JavaClass> boClasses = jarClasses.stream().filter(javaClass -> ResultType.TRUE.equals(predicate.apply(javaClass))).collect(Collectors.toList());
        assertFalse(boClasses.isEmpty(), "found business objects");

        Optional<JavaClass> catalogBOClass = jarClasses.stream().filter(javaClass -> javaClass.getClassName().equals("com.intershop.component.catalog.capi.CatalogBO")).findAny();
        assertTrue(catalogBOClass.isPresent(), "found catalogbo");

        assertEquals(ResultType.TRUE, predicate.apply("com.intershop.component.catalog.capi.CatalogCategoryBO"), "CatalogCategoryBO is business object");
        assertEquals(ResultType.WAIT, predicate.apply("com.intershop.component.catalog.capi.CatalogBO"), "CatalogBO is business object");
        assertEquals(ResultType.TRUE, predicate.apply(catalogBOClass.get()), "CatalogBO is business object");
        assertEquals(ResultType.TRUE, predicate.apply("com.intershop.component.catalog.capi.CatalogBO"), "CatalogBO is business object");
    }
}
