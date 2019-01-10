package com.intershop.tool.architecture.report.common.project;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import com.intershop.tool.architecture.versions.SemanticVersion;

public class ProjectRef implements Serializable
{
    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String group;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String version;

    public ProjectRef(String group, String name, String version)
    {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    public ProjectRef()
    {
    }

    @XmlTransient
    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    /**
     * @return project name without the group name (e.g. a cartridgeName)
     */
    @XmlTransient
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @XmlTransient
    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    /**
     * @return "group:name" of project
     */
    public String getIdentifier()
    {
        return group + ":" + name;
    }

    @Override
    public String toString()
    {
        return getIdentifier();
    }

    public int compareTo(ProjectRef other)
    {
        return this.getIdentifier().compareTo(other.getIdentifier());
    }

    public String getSemanticVersion()
    {
        return SemanticVersion.valueOf(getVersion()).getVersionWithoutBuildExtension();
    }
}
