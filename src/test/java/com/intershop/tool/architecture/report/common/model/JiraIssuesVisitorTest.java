package com.intershop.tool.architecture.report.common.model;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

public class JiraIssuesVisitorTest
{

    private static final String TEST_ISSUES = "test-issues.xml";
    private static final String TEST_DAMAGED = "test-issues-damaged.xml";

    @Test
    public void testReadCorrectFile() throws FileNotFoundException
    {
        JiraIssuesVisitor underTest = new JiraIssuesVisitor();
        List<JiraIssue> result = underTest.apply(getClass().getClassLoader().getResourceAsStream(TEST_ISSUES));
        assertEquals("correct size", 3, result.size());
        assertEquals("correct number","ENFINITY-17360", result.get(0).getJiraID());
    }

    @Test(expected=XMLLoaderException.class)
    public void testReadDamagedFile() throws FileNotFoundException
    {
        JiraIssuesVisitor underTest = new JiraIssuesVisitor();
        List<JiraIssue> result = underTest.apply(getClass().getClassLoader().getResourceAsStream(TEST_DAMAGED));
        assertEquals("correct number","ISTOOLS-4069", result.get(0).getJiraID());
    }
}
