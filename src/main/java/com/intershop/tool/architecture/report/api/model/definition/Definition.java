package com.intershop.tool.architecture.report.api.model.definition;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

import com.intershop.tool.architecture.report.common.project.ProjectRef;

public class Definition
{
    private String signature;
    @XmlAttribute
    private Artifact artifact;
    @XmlAttribute
    private String source;
    private ProjectRef projectRef;

    @XmlTransient
    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    @XmlTransient
    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact(Artifact artifact)
    {
        this.artifact = artifact;
    }

    public ProjectRef getProjectRef()
    {
        return projectRef;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
        result = prime * result + ((getSignature() == null) ? 0 : getSignature().hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
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
        Definition other = (Definition)obj;
        if (artifact != other.artifact)
            return false;
        if (signature == null)
        {
            if (other.signature != null)
                return false;
        }
        else if (!signature.equals(other.signature))
            return false;
        if (source == null)
        {
            if (other.source != null)
                return false;
        }
        else if (!source.equals(other.source))
            return false;
        return true;
    }

    public String getSignature()
    {
        return signature;
    }

    public void setSignature(String signature)
    {
        this.signature = signature;
    }

    public void setProjectRef(ProjectRef projectRef)
    {
        this.projectRef = projectRef;
    }
}
