package com.intershop.tool.architecture.report.pipeline.messages;

import java.io.Serializable;

public class PipelineTestResponse implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final PipelineTestRequest request;

    public PipelineTestResponse(PipelineTestRequest request)
    {
        this.request = request;
    }

    public PipelineTestRequest getRequest()
    {
        return request;
    }
}
