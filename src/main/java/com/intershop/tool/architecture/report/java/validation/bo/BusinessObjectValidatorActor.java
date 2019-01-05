package com.intershop.tool.architecture.report.java.validation.bo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.akka.actors.tooling.AkkaWaitingMessages;
import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.common.model.ValidationResult;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;
import com.intershop.tool.architecture.report.java.validation.po.IsPersistenceResponse;
import com.intershop.tool.architecture.report.java.validation.po.PersistenceClassPredicate;

import akka.actor.AbstractActor;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business objects only.
 */
public class BusinessObjectValidatorActor extends AbstractActor
{
    private final Map<String, Boolean> persistentClasses = new HashMap<>();
    private static final PersistenceClassPredicate staticPersistencePredicate = new PersistenceClassPredicate();
    private Function<String, ResultType> persistencePredicate = t -> {
        ResultType result = staticPersistencePredicate.apply(t);
        if (ResultType.WAIT.equals(result))
        {
            Boolean isPersistent = persistentClasses.get(t);
            if (isPersistent != null)
            {
                result = isPersistent ? ResultType.TRUE : ResultType.FALSE;
            }
        }
        if (result == null)
        {
            LoggerFactory.getLogger(getClass()).warn("BusinessObjectValidatorActor waiting for persistent class: '{}'.", t);
        }
        return result == null ? ResultType.WAIT : result;
    };
    private final BusinessObjectAPIValidator predicate = new BusinessObjectAPIValidator(persistencePredicate, t -> ResultType.TRUE);
    private static AkkaWaitingMessages<JavaClassRequest> waiting = new AkkaWaitingMessages<>();

    public void clear()
    {
        waiting.clear();
        staticPersistencePredicate.clear();
        persistentClasses.clear();
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(IsPersistenceResponse.class, this::receive)
                        .match(JavaClassRequest.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            clear();
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    private void receive(JavaClassRequest request)
    {
        ValidationResult issues = predicate.apply(request);
        if (ResultType.WAIT.equals(issues.getResultType()))
        {
            waiting.put(request, getSender(), getSelf());
        }
        else
        {
            getSender().tell(new ValidateBusinessObjectResponse(request, issues.getIssues()), getSelf());
        }
    }

    private void receive(IsPersistenceResponse response)
    {
        persistentClasses.put(response.getRequest().getJavaClass().getClassName(), response.isPersistent());
        // new persistence information received, waiting business objects could be validated now
        waiting.resend();
    }
}
