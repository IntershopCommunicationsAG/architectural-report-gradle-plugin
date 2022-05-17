package com.intershop.tool.architecture.report.common.model;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

import com.intershop.tool.architecture.report.common.issue.AzureIssue;
import com.intershop.tool.architecture.report.common.issue.AzureIssuesVisitor;
import com.intershop.tool.architecture.report.common.resources.XMLLoaderException;

public class AzureBoardIssuesVisitorTest
{

    private static final String TEST_ISSUES = "test-issues.xml";
    private static final String TEST_DAMAGED = "test-issues-damaged.xml";

    @Test
    public void testReadCorrectFile() throws FileNotFoundException
    {
        AzureIssuesVisitor underTest = new AzureIssuesVisitor();
        List<AzureIssue> result = underTest.apply(getClass().getClassLoader().getResourceAsStream(TEST_ISSUES));
        assertEquals("correct size", 3, result.size());
        assertEquals("correct number","ENFINITY-17360", result.get(0).getWorkItemID());
    }

    @Test(expected=XMLLoaderException.class)
    public void testReadDamagedFile() throws FileNotFoundException
    {
        AzureIssuesVisitor underTest = new AzureIssuesVisitor();
        List<AzureIssue> result = underTest.apply(getClass().getClassLoader().getResourceAsStream(TEST_DAMAGED));
        assertEquals("correct number","ISTOOLS-4069", result.get(0).getWorkItemID());
    }
}
