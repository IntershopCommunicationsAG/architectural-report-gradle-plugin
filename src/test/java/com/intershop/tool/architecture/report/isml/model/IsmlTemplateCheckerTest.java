package com.intershop.tool.architecture.report.isml.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.ProjectRef;

public class IsmlTemplateCheckerTest
{
    @Test
    public void testAllFine()
    {
        File file = new File(getClass().getClassLoader().getResource("FullSitePreviewNoPreviewFound.isml").getFile());
        IsmlTemplateChecker checker = new IsmlTemplateChecker(new ProjectRef("test.group", "test", "1.0"), file);
        List<Issue> issues = checker.getIssues();
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testAllFineLocalizedText()
    {
        File file = new File(getClass().getClassLoader().getResource("LocalizedText.isml").getFile());
        IsmlTemplateChecker checker = new IsmlTemplateChecker(new ProjectRef("test.group", "test", "1.0"), file);
        List<Issue> issues = checker.getIssues();
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testAllFineBusinessObjectID()
    {
        File file = new File(getClass().getClassLoader().getResource("BusinessObjectID.isml").getFile());
        IsmlTemplateChecker checker = new IsmlTemplateChecker(new ProjectRef("test.group", "test", "1.0"), file);
        List<Issue> issues = checker.getIssues();
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testEncodingOff()
    {
        File file = new File(getClass().getClassLoader().getResource("ProductCompare.isml").getFile());
        IsmlTemplateChecker checker = new IsmlTemplateChecker(new ProjectRef("test.group", "test", "1.0"), file);
        List<Issue> issues = checker.getIssues();
        assertEquals("found", 1, issues.size());
        Issue issue = issues.get(0);
        assertEquals("line", Long.valueOf(3), issue.getParameters()[1]);
        assertEquals("column", Integer.valueOf(1), issue.getParameters()[2]);
    }

}
