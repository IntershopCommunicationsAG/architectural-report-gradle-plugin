package com.intershop.tool.architecture.report.project.actors;

import java.io.File;
import java.util.Collection;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.isml.messages.GetIsmlTemplatesRequest;
import com.intershop.tool.architecture.report.isml.messages.GetIsmlTemplatesResponse;
import com.intershop.tool.architecture.report.isml.model.IsmlFinder;
import com.intershop.tool.architecture.report.jar.JarFinder;
import com.intershop.tool.architecture.report.pipeline.messages.GetPipelinesRequest;
import com.intershop.tool.architecture.report.pipeline.messages.GetPipelinesResponse;
import com.intershop.tool.architecture.report.pipeline.model.PipelineFinder;
import com.intershop.tool.architecture.report.project.messages.GetJarsRequest;
import com.intershop.tool.architecture.report.project.messages.GetJarsResponse;

import akka.actor.AbstractActor;

public class ProjectActor extends AbstractActor
{
    private File cartridgesDirectory = null;
    private JarFinder jarFinder = new JarFinder();
    private PipelineFinder pipelineFinder = new PipelineFinder();
    private IsmlFinder ismlFinder = new IsmlFinder();

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(CommandLineArguments.class, this::receive)
                        .match(GetJarsRequest.class, this::receive)
                        .match(GetPipelinesRequest.class, this::receive)
                        .match(GetIsmlTemplatesRequest.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    private void receive(CommandLineArguments message)
    {
        cartridgesDirectory = new File(message.getArgument(ArchitectureReportConstants.ARG_CARTRIDGE_DIRECTORY));
    }

    private void receive(GetPipelinesRequest request)
    {
        Collection<File> files = pipelineFinder.apply(new File(cartridgesDirectory, request.getProjectRef().getName() + "/release/pipelines"));
        getSender().tell(new GetPipelinesResponse(request, files), getSelf());
    }

    private void receive(GetJarsRequest request)
    {
        Collection<File> files = jarFinder.apply(new File(cartridgesDirectory, request.getProjectRef().getName() + "/release/lib"));
        getSender().tell(new GetJarsResponse(request, files), getSelf());
    }


    private void receive(GetIsmlTemplatesRequest request)
    {
        Collection<File> files = ismlFinder.apply(new File(cartridgesDirectory, request.getProjectRef().getName() + "/release/templates"));
        getSender().tell(new GetIsmlTemplatesResponse(request, files), getSelf());
    }
}
