package com.intershop.tool.architecture.report.pipeline.messages;

import java.io.Serializable;

import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.pipeline.model.Pipeline;

public class PipelineResponse implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final FileRequest request;
    private final Pipeline pipeline;

    public PipelineResponse(FileRequest request, Pipeline pipeline)
    {
        this.request = request;
        this.pipeline = pipeline;
    }

    public Pipeline getPipeline()
    {
        return pipeline;
    }

    public FileRequest getRequest()
    {
        return request;
    }
}
