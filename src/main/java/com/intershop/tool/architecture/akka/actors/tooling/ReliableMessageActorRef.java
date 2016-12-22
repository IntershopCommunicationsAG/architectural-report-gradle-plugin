package com.intershop.tool.architecture.akka.actors.tooling;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActorContext;

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
    private final ActorRef actor;
    private final ActorRef owner;
    private final AkkaWaitingMessages<T> waitingMessages = new AkkaWaitingMessages<>();
    private boolean isFinished = false;

    public ReliableMessageActorRef(UntypedActorContext context, Class<?> actorClass, ActorRef owner)
    {
        this.name = actorClass.getSimpleName();
        this.actor = context.actorOf(Props.create(actorClass));
        this.owner = owner;
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
        waitingMessages.put(message, owner, owner);
        actor.tell(message, owner);
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
     *
     * @return true if actor is waiting for answers
     */
    public boolean flush()
    {
        if (!isFinished)
        {
            logger.info("'{}' FLUSH received", name);
            actor.tell(AkkaMessage.TERMINATE.FLUSH_REQUEST, owner);
        }
        return waitingMessages.isEmpty();
    }

    public void flushResponse(ActorRef actorRef)
    {
        if (actorRef.equals(actor) || isFinished)
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
        actor.tell(message, owner);
    }
}
