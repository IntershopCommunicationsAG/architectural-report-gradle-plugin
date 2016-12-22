package com.intershop.tool.architecture.report.common.model;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

public class JiraIssuesVisitorTest
{

    private static final String TEST_ISSUES = "test-issues.xml";

    @Test
    public void test() throws FileNotFoundException
    {
        JiraIssuesVisitor underTest = new JiraIssuesVisitor();
        List<JiraIssue> result = underTest.apply(getClass().getClassLoader().getResourceAsStream(TEST_ISSUES));
        assertEquals("correct size", 3, result.size());
        assertEquals("correct number","ENFINITY-17360", result.get(0).getJiraID());
    }

}
