package com.intershop.tool.architecture.report.common.issue;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.intershop.tool.architecture.report.common.project.ProjectRef;

public class Issue implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final ProjectRef projectRef;
    private final String key;
    private final Object[] parameters;
    private final String hash;

    /**
     * @param projectRef
     *            project contains issues
     * @param key
     *            problem key (like com.intershop.businessobject.persistent)
     * @param parameters
     *            parameter of issue
     */
    public Issue(ProjectRef projectRef, String key, Object... parameters)
    {
        this.projectRef = projectRef;
        this.key = key;
        this.parameters = parameters;
        this.hash = calculateHash();
    }

    public String getKey()
    {
        return key;
    }

    public Object[] getParameters()
    {
        return parameters;
    }

    /**
     * @return an hash code, which can be used to reference the found issue
     */
    public String getHash()
    {
        return hash;
    }

    @Override
    public String toString()
    {
        return getProjectRef().toString() + ":" + getIssueString();
    }

    private String getIssueString()
    {
        return key + ":" + getParametersString();
    }

    public String getParametersString()
    {
        StringBuilder builder = new StringBuilder();
        if (parameters.length > 0)
        {
            builder.append(parameters[0]);
        }
        for (int i = 1; i < parameters.length; i++)
        {
            builder.append(",").append(parameters[i].toString());
        }
        return builder.toString();
    }

    private String calculateHash()
    {
        // calculate reproducible key
        String stringRepresentation = getIssueString();
        try
        {
            byte[] bytes = stringRepresentation.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] keyBytes = md.digest(bytes);

            BigInteger bigInt = new BigInteger(1, keyBytes);
            String s = bigInt.toString(16);

            // normalize to 32 characters, shouldn't happen very often...
            while(s.length() < 32)
            {
                s = "0" + s;
            }

            return s;
        }
        catch(UnsupportedEncodingException | NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Can't create MD5 hash for:" + stringRepresentation, e);
        }
    }

    public ProjectRef getProjectRef()
    {
        return projectRef;
    }

}
