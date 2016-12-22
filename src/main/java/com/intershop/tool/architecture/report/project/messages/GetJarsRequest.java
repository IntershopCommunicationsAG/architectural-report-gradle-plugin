package com.intershop.tool.architecture.report.project.messages;

import java.io.Serializable;

import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class GetJarsRequest implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final ProjectRef projectRef;

    public GetJarsRequest(ProjectRef projectRef)
    {
        this.projectRef = projectRef;
    }

    public ProjectRef getProjectRef()
    {
        return projectRef;
    }


}
