package com.intershop.tool.architecture.report.common.project;

import java.util.function.Function;

import com.intershop.tool.architecture.report.api.model.definition.Artifact;
import com.intershop.tool.architecture.report.api.model.definition.Definition;

public class LibDefinitionMapper implements Function<ProjectRef, Definition>
{
    public static final String API_SOURCE_IVY_XML = "ivy.xml";
    private final ProjectRef serverProject;

    public LibDefinitionMapper(ProjectRef serverProject)
    {
        this.serverProject = serverProject;
    }

    @Override
    public Definition apply(ProjectRef project)
    {
        Definition apiDef = new Definition();
        apiDef.setProjectRef(serverProject);
        apiDef.setArtifact(Artifact.LIBRARY);
        apiDef.setSource(API_SOURCE_IVY_XML);
        apiDef.setSignature(project.getIdentifier() + "=" + project.getSemanticVersion());
        return apiDef;
    }

}
