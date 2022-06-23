package com.intershop.tool.architecture.report.common.model;

import com.intershop.tool.architecture.report.common.issue.AzureIssue;
import com.intershop.tool.architecture.report.common.issue.AzureIssuesVisitor;
import com.intershop.tool.architecture.report.common.resources.XMLLoaderException;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzureBoardIssuesVisitorTest
{
    private static final String TEST_ISSUES = "test-issues.xml";
    private static final String TEST_DAMAGED = "test-issues-damaged.xml";

    @Test
    public void testReadCorrectFile() throws FileNotFoundException
    {
        AzureIssuesVisitor underTest = new AzureIssuesVisitor();
        List<AzureIssue> result = underTest.apply(getClass().getClassLoader().getResourceAsStream(TEST_ISSUES));
        assertEquals(3, result.size(), "correct size");
        assertEquals("ENFINITY-17360", result.get(0).getWorkItemID(), "correct number");
    }

    @Test
    public void testReadDamagedFile() throws FileNotFoundException, XMLLoaderException
    {
        AzureIssuesVisitor underTest = new AzureIssuesVisitor();
        Exception exception = assertThrows(XMLLoaderException.class, () -> {
            List<AzureIssue> result = underTest.apply(getClass().getClassLoader().getResourceAsStream(TEST_DAMAGED));
            assertEquals("ISTOOLS-4069", result.get(0).getWorkItemID(), "correct number");
        });
        assertEquals("Can't import existing issues", exception.getMessage());
    }
}
