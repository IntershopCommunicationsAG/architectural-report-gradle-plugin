package com.intershop.tool.architecture.report.java.model.jclass;

import com.intershop.tool.architecture.report.api.model.definition.APIDefinition;
import com.intershop.tool.architecture.report.api.model.definition.Artifact;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.common.resources.ResourceLoader;
import com.intershop.tool.architecture.report.common.resources.XmlLoader;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JavaClassVisitorTest
{
    private static final String TEST_JAR = "test_co.jar";
    private JarFileVisitor jarVisitor = new JarFileVisitor(new ProjectRef("test.group", "test", "1.0"));

    @Test
    public void testFieldAPIDefinition()
    {
        Collection<JavaClass> classes = jarVisitor.getClasses(TEST_JAR);
        JavaClass capiClass = classes.parallelStream().filter(c -> c.getClassName().equals("com.intershop.beehive.core.capi.businessobject.BusinessObjectMgr")).findFirst().get();
        assertFalse(capiClass.getApiDefinition().isEmpty(), "definition found");
        Definition fieldDef = capiClass.getApiDefinition().stream().filter(d -> Artifact.PUBLIC_FIELD.equals(d.getArtifact())).findFirst().get();
        assertEquals("REGISTRY_NAME", fieldDef.getSignature(), "public member found");
    }

    @Test
    public void testMethodAPIDefinition()
    {
        Collection<JavaClass> classes = jarVisitor.getClasses(TEST_JAR);
        JavaClass capiClass = classes.parallelStream().filter(c -> c.getClassName().equals("com.intershop.beehive.core.capi.businessobject.BusinessObjectMgr")).findFirst().get();
        List<Definition> methods = capiClass.getApiDefinition().stream().filter(d -> Artifact.PUBLIC_METHOD.equals(d.getArtifact())).collect(Collectors.toList());
        assertEquals(1, methods.size(), "public methods found");
    }

    @Test
    public void testGetSignature()
    {
        assertEquals("com.intershop.beehive.core.capi.businessevent.BusinessEventDefinition createBusinessEventDefinition(java.lang.String,java.lang.String,java.lang.String)", JavaClassVisitor.getSignature("AnyClass", "createBusinessEventDefinition", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/intershop/beehive/core/capi/businessevent/BusinessEventDefinition;"), "with parameter");
        assertEquals("AnyClass()", JavaClassVisitor.getSignature("AnyClass", "<init>", "()"), "constructor");
    }

    @Test
    public void testExport() throws IOException, JAXBException
    {
        XmlLoader loader = new XmlLoader();
        Collection<JavaClass> classes = jarVisitor.getClasses(TEST_JAR);
        APIDefinition xmlModel = new APIDefinition();
        classes.stream().forEach(jc -> xmlModel.getDefinition().addAll(jc.getApiDefinition()));
        StringWriter writer = new StringWriter();
        loader.exportXML(xmlModel, writer);
        assertEquals(ResourceLoader.getString("api_definition_test_co.xml").replace("\r", ""), writer.toString().replace("\r", ""), "export correct");
    }
}
