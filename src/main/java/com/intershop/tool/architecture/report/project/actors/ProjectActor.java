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

import akka.actor.UntypedActor;

public class ProjectActor extends UntypedActor
{
    private File cartridgesDirectory = null;
    private JarFinder jarFinder = new JarFinder();
    private PipelineFinder pipelineFinder = new PipelineFinder();
    private IsmlFinder ismlFinder = new IsmlFinder();

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof CommandLineArguments)
        {
            cartridgesDirectory = new File(((CommandLineArguments)message).getArgument(ArchitectureReportConstants.ARG_CARTRIDGE_DIRECTORY));
        }
        else if (message instanceof GetJarsRequest)
        {
            receive((GetJarsRequest)message);
        }
        else if (message instanceof GetPipelinesRequest)
        {
            receive((GetPipelinesRequest)message);
        }
        else if (message instanceof GetIsmlTemplatesRequest)
        {
            receive((GetIsmlTemplatesRequest)message);
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
