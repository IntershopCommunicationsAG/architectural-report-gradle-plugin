package com.intershop.tool.architecture.report.jar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.junit.Test;

import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.model.pipelet.PipeletDescriptor;

public class JarFileVisitorTest
{
    private static final String CA_JAR = "test_ca.jar";
    private static final String CO_JAR = "test_co.jar";

    private JarFileVisitor underTest = new JarFileVisitor(new ProjectRef("test.group", "test", "1.0"));

    @Test
    public void testCatalogJar() throws IOException
    {
        Collection<JavaClass> classes = underTest.getClasses(CA_JAR);
        assertEquals("number of classes", 18, classes.size());
        JavaClass catalogBOClass = findClass("com.intershop.component.catalog.capi.CatalogBO", classes);
        assertNotNull("found CatalogBO class", catalogBOClass);
        assertTrue("find return type",
                        catalogBOClass.getUsageRefs().contains(
                                        "com.intershop.component.catalog.capi.CatalogBORepository"));
        assertTrue("domain is used at deprecated method",
                        catalogBOClass.getDeprecatedRefs().contains("com.intershop.beehive.core.capi.domain.Domain"));
        assertFalse("domain is used at deprecated method only",
                        catalogBOClass.getUsageRefs().contains("com.intershop.beehive.core.capi.domain.Domain"));
        assertTrue("parameter of deprecated method",
                        catalogBOClass.getDeprecatedRefs().contains(
                                        "com.intershop.component.mvc.capi.catalog.ClassificationSystem"));
    }

    @Test
    public void testJarClasses() throws IOException
    {
        Collection<JavaClass> classes = underTest.getClasses(CA_JAR);
        assertEquals("number of classes", 18, classes.size());
        JavaClass applicationBO = findClass("com.intershop.component.catalog.capi.CatalogCategoryBO", classes);
        assertNotNull("found ApplicationBO class", applicationBO);
        assertTrue("domain is used at deprecated method",
                        applicationBO.getDeprecatedRefs().contains("com.intershop.beehive.core.capi.domain.Domain"));
        assertTrue("domain is still used at getSite()",
                        applicationBO.getUsageRefs().contains("com.intershop.beehive.core.capi.domain.Domain"));
    }

    @Test
    public void testJarPipelets() throws IOException
    {
        Collection<PipeletDescriptor> classes = underTest.getPipeletDescriptors(CO_JAR);
        assertEquals("number of classes", 125, classes.size());
        PipeletDescriptor pipeletDesc = findPipelet("enfinity:/test/pipelets/InvalidateBusinessObject.xml", classes);
        assertNotNull("found GetApplicationBO pipelets", pipeletDesc);
        assertEquals("class file correct", "com.intershop.beehive.core.pipelet.businessobject.InvalidateBusinessObject", pipeletDesc.getPipeletClassName());
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
