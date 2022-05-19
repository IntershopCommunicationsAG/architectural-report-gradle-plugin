package com.intershop.tool.architecture.report.common.issue;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Visits a Azure configuration XML file.
 */
public class AzureIssuesHandler extends DefaultHandler
{
    private final List<AzureIssue> issues;

    private boolean inAzure;
    private boolean inBoard;

    private AzureBoard currentAzureBoard;

    /**
     * The constructor.
     * @param issues list of issue to be filled with this import handler
     */
    public AzureIssuesHandler(List<AzureIssue> issues)
    {
        this.issues = issues;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if ("azure".equals(qName))
        {
            inAzure = true;
        }
        else if (inAzure && "board".equals(qName))
        {
            String name = attributes.getValue("name");
            String url = attributes.getValue("url");

            currentAzureBoard = new AzureBoard(name, url);

            inBoard = true;
        }
        else if (inBoard && "issue".equals(qName))
        {
            String key = attributes.getValue("key");
            String workItemID = attributes.getValue("work-item-id");

            AzureIssue issue = new AzureIssue(currentAzureBoard, key, workItemID);
            issues.add(issue);
        }
        else
        {
            throw new SAXException("Unknown element or wrong structure");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if ("azure".equals(qName))
        {
            inAzure = false;
        }
        else if (inAzure && "board".equals(qName))
        {
            currentAzureBoard = null;
            inBoard = false;
        }
    }
}
