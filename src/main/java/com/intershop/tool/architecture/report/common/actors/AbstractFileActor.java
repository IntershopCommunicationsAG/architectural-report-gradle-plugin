package com.intershop.tool.architecture.report.common.actors;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.common.messages.FileRequest;

import akka.actor.AbstractActor;

/**
 * Receives {@link FileRequest} messages and delegates this message to {@link #receive(FileRequest)}
 */
public abstract class AbstractFileActor extends AbstractActor
{
    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(FileRequest.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    abstract protected void receive(FileRequest message);
}
