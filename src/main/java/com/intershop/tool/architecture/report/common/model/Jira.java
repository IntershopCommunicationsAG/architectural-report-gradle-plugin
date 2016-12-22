package com.intershop.tool.architecture.report.common.model;

/**
 * Represents a Jira instance which contains Jira issues.
 */
public class Jira
{
    private String name;
    private String url;

    /**
     * The constructor.
     * @param name name of jira server
     * @param url url to jira server
     */
    public Jira(String name, String url)
    {
        this.name = name;
        this.url = url;
    }

    public String getName()
    {
        return name;
    }

    public String getURLPrefix()
    {
        return url;
    }
}
