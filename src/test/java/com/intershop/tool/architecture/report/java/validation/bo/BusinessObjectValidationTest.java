package com.intershop.tool.architecture.report.java.validation.bo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.intershop.tool.architecture.report.common.issue.ResultType;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.JarFileVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;

public class BusinessObjectValidationTest
{
    private static final ProjectRef PROJECT_REF = new ProjectRef("test.group", "test", "1.0");
    private static final String TEST_JAR = "test_ca.jar";
    private JarFileVisitor jarVisitor = new JarFileVisitor(PROJECT_REF);
    private List<String> wrongClasses = Arrays.asList("com.intershop.component.catalog.capi.CatalogBORepositoryExtensionFactory",
                    "com.intershop.component.catalog.capi.CatalogCategoryBO");
    @Test
    public void test() throws IOException
    {
        Collection<JavaClass> boClasses = jarVisitor.getClasses(TEST_JAR);

        Function<String, ResultType> persistencePredicate = t -> t.contains("PO") || t.contains("Domain")
                        || t.contains(".orm.") ? ResultType.TRUE : ResultType.FALSE;
        Function<JavaClass, ResultType> businessObjectPredicate = t -> t.getClassName().contains("BO") ? ResultType.TRUE
                        : ResultType.FALSE;
        BusinessObjectAPIValidator underTest = new BusinessObjectAPIValidator(persistencePredicate,
                        businessObjectPredicate);
        for (JavaClass javaClass : boClasses)
        {
            if (wrongClasses.contains(javaClass.getClassName()))
            {
                assertEquals("CatalogBORepositoryExtensionFactory", ResultType.FALSE,
                                underTest.validate(PROJECT_REF, javaClass).getResultType());
            }
            else
            {
                assertEquals("business object is fine:" + javaClass.getClassName(), ResultType.TRUE,
                                underTest.validate(PROJECT_REF, javaClass).getResultType());
            }
        }
    }

}
