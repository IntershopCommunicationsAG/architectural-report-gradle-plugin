package com.intershop.tool.architecture.report.java.validation.capi;

import java.util.function.Function;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.common.model.ValidationResult;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

import akka.actor.UntypedActor;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business objects only.
 */
public class CapiValidatorActor extends UntypedActor
{
    private final Function<JavaClassRequest, ValidationResult> validator = new CapiUsingInternalValidator();

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
        ValidationResult result = validator.apply(request);
        if (ResultType.TRUE.equals(result.getResultType()))
        {
            getSender().tell(new ValidateCapiResponse(request, result.getIssues()), getSelf());
        }
    }
}
