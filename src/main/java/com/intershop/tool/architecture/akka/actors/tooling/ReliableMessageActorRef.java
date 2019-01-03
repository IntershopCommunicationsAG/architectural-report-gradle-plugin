package com.intershop.tool.architecture.akka.actors.tooling;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Akka actor reference to an actor, which handles one type of message. This
 * actor reference provides reliable messaging to the actor.
 *
 * @param <T>
 *            message type
 */
public class ReliableMessageActorRef<T>
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String name;
    private final ActorRef receiver;
    private final ActorRef sender;
    private final AkkaWaitingMessages<T> waitingMessages = new AkkaWaitingMessages<>();
    private boolean isFinished = false;

    public ReliableMessageActorRef(ActorContext context, Class<?> receiverClass, ActorRef sender)
    {
        this.name = receiverClass.getSimpleName();
        this.receiver = context.actorOf(Props.create(receiverClass));
        this.sender = sender;
    }

    public void tell(Collection<T> messages)
    {
        for (T message : messages)
        {
            tell(message);
        }
    }

    /**
     * tells the actor the given message and registers the message at message queue to observe execution of message
     *
     * @param message message for actor
     */
    public void tell(T message)
    {
        AkkaMessage<T> akkaMessage = waitingMessages.put(message, sender, receiver);
        akkaMessage.send();
        isFinished = false;
    }

    /**
     * Mark message as received (remove message from open messages)
     *
     * @param message received result for message
     */
    public void receive(T message)
    {
        waitingMessages.remove(message);
    }

    /**
     * tells the actor to finish
     */
    public void flush()
    {
        if (!isFinished)
        {
            logger.debug("'{}' FLUSH received", name);
            receiver.tell(AkkaMessage.TERMINATE.FLUSH_REQUEST, sender);
        }
    }

    public void flushResponse(ActorRef actorRef)
    {
        if (actorRef.equals(receiver) || isFinished)
        {
            if (!waitingMessages.resend())
            {
                logger.info("'{}' FINISH received", name);
                isFinished = true;
            }
        }
    }

    public boolean isFinished()
    {
        return isFinished;
    }

    public void tellOtherMessage(Object message)
    {
        receiver.tell(message, sender);
    }
}
