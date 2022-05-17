package com.intershop.tool.architecture.report.common.issue;

/**
 * Represents an Azure board instance which contains issues.
 */
public class AzureBoard
{
    private final String name;
    private final String url;

    /**
     * The constructor.
     * @param name Name of Azure board
     * @param url URL to Azure board
     */
    public AzureBoard(String name, String url)
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
