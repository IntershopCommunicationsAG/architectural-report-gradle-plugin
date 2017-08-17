package com.intershop.tool.architecture.report.project.messages;

import java.util.Collection;
import java.util.List;

import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class GetProjectsResponse
{
    private final GetProjectsRequest request;
    private final Collection<ProjectRef> projects;
    private List<Definition> definitions;

    public GetProjectsResponse(GetProjectsRequest request, Collection<ProjectRef> projects,
                    List<Definition> definitions)
    {
        this.projects = projects;
        this.request = request;
        this.setDefinitions(definitions);
    }

    public GetProjectsRequest getRequest()
    {
        return request;
    }

    public Collection<ProjectRef> getProjects()
    {
        return projects;
    }

    public List<Definition> getDefinitions()
    {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions)
    {
        this.definitions = definitions;
    }
}
