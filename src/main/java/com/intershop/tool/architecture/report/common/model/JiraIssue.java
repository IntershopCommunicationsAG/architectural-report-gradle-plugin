package com.intershop.tool.architecture.report.common.model;

/**
 * Represents an issue in Jira.
 */
public class JiraIssue
{
    private Jira jira;
    private String key;
    private String jiraID;

    /**
     * The constructor.
     *
     * @param jira
     *            jira server
     * @param key
     *            issue hash key
     * @param jiraID
     *            jira issue identifier
     *
     */
    public JiraIssue(Jira jira, String key, String jiraID)
    {
        this.jira = jira;
        this.key = key;
        this.jiraID = jiraID;
    }

    /**
     * Returns the Jira to which the issue belongs.
     * @return jira server
     */
    public Jira getJira()
    {
        return jira;
    }

    /**
     * Returns the problem key which is associated with the Jira issue.
     * @return issue hash key
     */
    public String getKey()
    {
        return key;
    }

    public String getJiraID()
    {
        return jiraID;
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
        JiraIssue other = (JiraIssue)obj;
        if (key == null)
        {
            if (other.key != null)
                return false;
        }
        else if (!key.equals(other.key))
            return false;
        return true;
    }

}
