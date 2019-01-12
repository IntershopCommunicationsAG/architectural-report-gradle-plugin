package com.intershop.tool.architecture.report.project.messages;

import java.util.Collection;

import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class GetProjectsResponse
{
    private final GetProjectsRequest request;
    private final Collection<ProjectRef> projects;
    private Collection<Definition> definitions;

    public GetProjectsResponse(GetProjectsRequest request, Collection<ProjectRef> projects,
                    Collection<Definition> definitions)
    {
        this.projects = projects;
        this.request = request;
        this.definitions = definitions;
    }

    public GetProjectsRequest getRequest()
    {
        return request;
    }

    public Collection<ProjectRef> getProjects()
    {
        return projects;
    }

    public Collection<Definition> getDefinitions()
    {
        return definitions;
    }
}
