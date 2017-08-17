package com.intershop.tool.architecture.report.project.model;

import java.util.function.Function;

import com.intershop.tool.architecture.report.api.model.definition.Artifact;
import com.intershop.tool.architecture.report.api.model.definition.Definition;

public class LibDefinitionMapper implements Function<ProjectRef, Definition>
{
    public static final String API_SOURCE_IVY_XML = "ivy.xml";
    private final static ProjectRef SERVER_PROJECT = new ProjectRef("SERVER", API_SOURCE_IVY_XML, "LOCAL");

    @Override
    public Definition apply(ProjectRef project)
    {
        Definition apiDef = new Definition();
        apiDef.setProjectRef(SERVER_PROJECT);
        apiDef.setArtifact(Artifact.LIBRARY);
        apiDef.setSource(API_SOURCE_IVY_XML);
        apiDef.setSignature(project.getIdentifier() + "=" + project.getVersion());
        return apiDef;
    }

}
