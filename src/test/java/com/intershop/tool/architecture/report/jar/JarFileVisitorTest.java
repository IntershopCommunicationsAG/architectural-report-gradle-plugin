package com.intershop.tool.architecture.report.jar;

import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.model.pipelet.PipeletDescriptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JarFileVisitorTest
{
    private static final String CA_JAR = "test_ca.jar";
    private static final String CO_JAR = "test_co.jar";

    private JarFileVisitor underTest = new JarFileVisitor(new ProjectRef("test.group", "test", "1.0"));

    @Test
    public void testCatalogJar() throws IOException
    {
        Collection<JavaClass> classes = underTest.getClasses(CA_JAR);
        assertEquals(18, classes.size(), "number of classes");
        JavaClass catalogBOClass = findClass("com.intershop.component.catalog.capi.CatalogBO", classes);
        assertNotNull(catalogBOClass, "found CatalogBO class");
        assertTrue(catalogBOClass.getUsageRefs().contains("com.intershop.component.catalog.capi.CatalogBORepository"), "find return type");
        assertTrue(catalogBOClass.getDeprecatedRefs().contains("com.intershop.beehive.core.capi.domain.Domain"), "domain is used at deprecated method");
        assertFalse(catalogBOClass.getUsageRefs().contains("com.intershop.beehive.core.capi.domain.Domain"), "domain is used at deprecated method only");
        assertTrue(catalogBOClass.getDeprecatedRefs().contains("com.intershop.component.mvc.capi.catalog.ClassificationSystem"), "parameter of deprecated method");
    }

    @Test
    public void testJarClasses() throws IOException
    {
        Collection<JavaClass> classes = underTest.getClasses(CA_JAR);
        assertEquals(18, classes.size(), "number of classes");
        JavaClass applicationBO = findClass("com.intershop.component.catalog.capi.CatalogCategoryBO", classes);
        assertNotNull(applicationBO, "found ApplicationBO class");
        assertTrue(applicationBO.getDeprecatedRefs().contains("com.intershop.beehive.core.capi.domain.Domain"), "domain is used at deprecated method");
        assertTrue(applicationBO.getUsageRefs().contains("com.intershop.beehive.core.capi.domain.Domain"), "domain is still used at getSite()");
    }

    @Test
    public void testJarPipelets() throws IOException
    {
        Collection<PipeletDescriptor> classes = underTest.getPipeletDescriptors(CO_JAR);
        assertEquals(125, classes.size(), "number of classes");
        PipeletDescriptor pipeletDesc = findPipelet("enfinity:/test/pipelets/InvalidateBusinessObject.xml", classes);
        assertNotNull(pipeletDesc, "found GetApplicationBO pipelets");
        assertEquals("com.intershop.beehive.core.pipelet.businessobject.InvalidateBusinessObject", pipeletDesc.getPipeletClassName(), "class file correct");
    }

    private static JavaClass findClass(String className, Collection<JavaClass> classes)
    {
        Optional<JavaClass> findResult = classes.stream().filter(jc -> jc.getClassName().equals(className)).findAny();
        return findResult.isPresent() ? findResult.get() : null;
    }

    private static PipeletDescriptor findPipelet(String pipeletRef, Collection<PipeletDescriptor> classes)
    {
        Optional<PipeletDescriptor> findResult = classes.stream().filter(jc -> pipeletRef.equals(jc.getReferenceName())).findAny();
        return findResult.isPresent() ? findResult.get() : null;
    }
}
