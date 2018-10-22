package com.intershop.tool.architecture.akka.actors.tooling;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;

public class AkkaWaitingMessages<T>
{
    private final Map<T, AkkaMessage<T>> waiting = new HashMap<>();

    public AkkaMessage<T> put(T message, ActorRef sender, ActorRef receiver)
    {
        AkkaMessage<T> akkaMessage = new AkkaMessage<T>(message, sender, receiver);
        waiting.put(message, akkaMessage);
        return akkaMessage;
    }

    /**
     * @return true in case a message was send
     */
    public boolean resend()
    {
        boolean result = false;
        synchronized(waiting)
        {
            if (!waiting.isEmpty())
            {
                for (AkkaMessage<T> message : waiting.values())
                {
                    message.send();
                    result = true;
                }
                waiting.clear();
            }
        }
        return result;
    }

    public boolean isEmpty()
    {
        return waiting.isEmpty();
    }

    public AkkaMessage<T> remove(T message)
    {
        return waiting.remove(message);
    }

    public int size()
    {
        return waiting.size();
    }

    public Collection<AkkaMessage<T>> getMessages()
    {
        return waiting.values();
    }

    public void resend(List<T> messages)
    {
        synchronized(waiting)
        {
            for (T key: messages)
            {
                AkkaMessage<T> message = waiting.get(key);
                if (message != null)
                {
                    message.send();
                }
            }
        }
    }
}
