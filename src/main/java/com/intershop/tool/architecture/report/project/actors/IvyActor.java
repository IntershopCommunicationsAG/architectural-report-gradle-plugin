package com.intershop.tool.architecture.report.project.actors;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.project.messages.GetProjectsRequest;
import com.intershop.tool.architecture.report.project.messages.GetProjectsResponse;
import com.intershop.tool.architecture.report.project.model.IvyVisitor;
import com.intershop.tool.architecture.report.project.model.LibDefinitionMapper;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

import akka.actor.UntypedActor;

public class IvyActor extends UntypedActor
{
    private static final IvyVisitor IVY_VISITOR = new IvyVisitor();
    private static final LibDefinitionMapper DEFINITION_MAPPER = new LibDefinitionMapper();

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof GetProjectsRequest)
        {
            receive((GetProjectsRequest)message);
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

    private void receive(GetProjectsRequest message)
    {
        Collection<ProjectRef> projects = IVY_VISITOR.apply(new File(message.getIvyFile()));
        List<Definition> definitions = projects.stream().map(DEFINITION_MAPPER).collect(Collectors.toList());
        getSender().tell(new GetProjectsResponse(message, projects, definitions), getSelf());
    }
}
