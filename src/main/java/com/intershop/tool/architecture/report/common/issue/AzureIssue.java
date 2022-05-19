package com.intershop.tool.architecture.report.common.issue;

/**
 * Represents an issue in an Azure board.
 */
public class AzureIssue
{
    private final AzureBoard azureBoard;
    private final String key;
    private final String workItemID;

    /**
     * The constructor.
     *
     * @param azureBoard
     *            Azure board
     * @param key
     *            issue hash key
     * @param workItemID
     *            Work item identifier
     *
     */
    public AzureIssue(AzureBoard azureBoard, String key, String workItemID)
    {
        this.azureBoard = azureBoard;
        this.key = key;
        this.workItemID = workItemID;
    }

    /**
     * Returns the Azure board to which the issue belongs.
     * @return Azure board
     */
    public AzureBoard getAzureBoard()
    {
        return azureBoard;
    }

    /**
     * Returns the problem key which is associated with the Azure issue.
     * @return issue hash key
     */
    public String getKey()
    {
        return key;
    }

    public String getWorkItemID()
    {
        return workItemID;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AzureIssue other = (AzureIssue)obj;
        if (key == null)
        {
            return other.key == null;
        }
        else return key.equals(other.key);
    }

}
