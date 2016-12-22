/*
 * JiraIssuesVisitor.java
 *
 * Copyright (c) 2012 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.common.model;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Visits a Jira configuration XML file.
 */
public class JiraIssuesHandler extends DefaultHandler
{
    private List<JiraIssue> issues;

    private boolean inJiraIssues;
    private boolean inJira;

    private Jira currentJira;

    /**
     * The constructor.
     * @param issues list of issue to be filled with this import handler
     */
    public JiraIssuesHandler(List<JiraIssue> issues)
    {
        this.issues = issues;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if ("jira-issues".equals(qName))
        {
            inJiraIssues = true;
        }
        else if (inJiraIssues && "jira".equals(qName))
        {
            String name = attributes.getValue("name");
            String url = attributes.getValue("url");

            currentJira = new Jira(name, url);

            inJira = true;
        }
        else if (inJira && "jira-issue".equals(qName))
        {
            String key = attributes.getValue("key");
            String jiraID = attributes.getValue("jira-id");

            JiraIssue issue = new JiraIssue(currentJira, key, jiraID);
            issues.add(issue);
        }
        else
        {
            throw new SAXException("unknown element or wrong structure");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if ("jira-issues".equals(qName))
        {
            inJiraIssues = false;
        }
        else if (inJiraIssues && "jira".equals(qName))
        {
            currentJira = null;
            inJira = false;
        }
    }
}
