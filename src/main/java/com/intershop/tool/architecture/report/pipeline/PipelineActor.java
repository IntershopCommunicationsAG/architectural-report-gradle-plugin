package com.intershop.tool.architecture.report.pipeline;

import java.io.File;
import java.util.function.Function;

import com.intershop.tool.architecture.report.common.actors.AbstractFileActor;
import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.pipeline.messages.PipelineResponse;
import com.intershop.tool.architecture.report.pipeline.model.Pipeline;
import com.intershop.tool.architecture.report.pipeline.model.PipelineVisitor;

public class PipelineActor extends AbstractFileActor
{
    private Function<File, Pipeline> visitor = new PipelineVisitor();

    @Override
    protected void receive(FileRequest request)
    {
        Pipeline pipeline = visitor.apply(new File(request.getFileName()));
        getSender().tell(new PipelineResponse(request, pipeline), getSelf());
    }
}
