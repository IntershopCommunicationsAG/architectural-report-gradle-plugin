package com.intershop.tool.architecture.report.common.actors;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

import akka.actor.AbstractActor;

/**
 * Receives {@link JavaClassRequest} messages and delegates this message to {@link #receive(JavaClassRequest)}
 */
public abstract class AbstractJavaClassActor extends AbstractActor
{
    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(JavaClassRequest.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    abstract protected void receive(JavaClassRequest message);
}
