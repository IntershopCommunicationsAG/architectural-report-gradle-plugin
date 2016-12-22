package com.intershop.tool.architecture.report.pipeline;

import java.io.File;
import java.util.function.Function;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.pipeline.messages.PipelineResponse;
import com.intershop.tool.architecture.report.pipeline.model.Pipeline;
import com.intershop.tool.architecture.report.pipeline.model.PipelineVisitor;

import akka.actor.UntypedActor;

public class PipelineActor extends UntypedActor
{
    Function<File, Pipeline> visitor = new PipelineVisitor();

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof FileRequest)
        {
            receive((FileRequest)message);
        }
        else if (AkkaMessage.TERMINATE.FLUSH_REQUEST.equals(message))
        {
            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
        }
        else
        {
            unhandled(message);
        }
    }

    private void receive(FileRequest request)
    {
        Pipeline pipeline = visitor.apply(new File(request.getFileName()));
        getSender().tell(new PipelineResponse(request, pipeline), getSelf());
    }
}
