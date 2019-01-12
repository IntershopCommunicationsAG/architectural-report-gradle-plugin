package com.intershop.tool.architecture.report.project.actors;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.project.messages.GetProjectsRequest;
import com.intershop.tool.architecture.report.project.messages.GetProjectsResponse;
import com.intershop.tool.architecture.report.project.model.IvyVisitor;
import com.intershop.tool.architecture.report.project.model.LibDefinitionMapper;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

import akka.actor.AbstractActor;

public class IvyActor extends AbstractActor
{
    private static final IvyVisitor IVY_VISITOR = new IvyVisitor();
    private static final LibDefinitionMapper DEFINITION_MAPPER = new LibDefinitionMapper();

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(GetProjectsRequest.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    private void receive(GetProjectsRequest message)
    {
        Collection<ProjectRef> projects = IVY_VISITOR.apply(new File(message.getIvyFile()));
        Set<Definition> definitions = projects.stream().map(DEFINITION_MAPPER).collect(Collectors.toSet());
        getSender().tell(new GetProjectsResponse(message, projects, definitions), getSelf());
    }
}
