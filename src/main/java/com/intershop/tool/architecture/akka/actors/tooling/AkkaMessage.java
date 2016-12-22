package com.intershop.tool.architecture.akka.actors.tooling;

import akka.actor.ActorRef;

public class AkkaMessage<T>
{
    public enum TERMINATE { FLUSH_REQUEST, FLUSH_RESPONSE, HARD };

    private final T message;
    private final ActorRef receiver;
    private final ActorRef sender;

    public AkkaMessage(T message, ActorRef sender, ActorRef receiver)
    {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
    }

    public void send()
    {
        receiver.tell(message, sender);
    }
}
