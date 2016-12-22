package com.intershop.tool.architecture.report.pipeline.messages;

import java.io.Serializable;

import com.intershop.tool.architecture.report.pipeline.model.Pipeline;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class PipelineTestRequest implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final Pipeline pipeline;
    private final ProjectRef projectRef;

    public PipelineTestRequest(Pipeline pipeline, ProjectRef projectRef)
    {
        this.projectRef = projectRef;
        this.pipeline = pipeline;
    }

    public Pipeline getPipeline()
    {
        return pipeline;
    }

    public ProjectRef getProjectRef()
    {
        return projectRef;
    }
}
