/**
 */
package com.intershop.tool.architecture.report.api.model.definition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class APIDefinition
{
    private List<Definition> definition= new ArrayList<>();
    private String version;
    private Date creationDate;

    public List<Definition> getDefinition()
    {
        return definition;
    }

    public void setDefinition(List<Definition> definition)
    {
        this.definition = definition;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }
}
