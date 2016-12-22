package com.intershop.tool.architecture.report.java.validation.bo.identification;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

import akka.actor.UntypedActor;

public class BusinessObjectFilterActor extends UntypedActor
{
    private final BusinessObjectPredicate predicate = new BusinessObjectPredicate();

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof JavaClassRequest)
        {
            JavaClassRequest request = (JavaClassRequest)message;
            onReceive(request);
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

    private void onReceive(JavaClassRequest request)
    {
        JavaClass javaClass = request.getJavaClass();
        ResultType result = predicate.apply(javaClass);
        if (!ResultType.WAIT.equals(result))
        {
            getSender().tell(new IsBusinessObjectResponse(request, ResultType.TRUE.equals(result)), getSelf());
        }
    }
}
