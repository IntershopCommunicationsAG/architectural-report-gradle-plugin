package com.intershop.tool.architecture.report.java.validation.po;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.akka.actors.tooling.AkkaWaitingMessages;
import com.intershop.tool.architecture.report.common.DependencyMap;
import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;
import com.intershop.tool.architecture.report.java.model.WaitForJavaClassResult;

import akka.actor.UntypedActor;

public class PersistenceFilterActor extends UntypedActor
{
    private PersistenceClassPredicate predicate = new PersistenceClassPredicate();
    private AkkaWaitingMessages<JavaClassRequest> waiting = new AkkaWaitingMessages<>();
    private DependencyMap<JavaClassRequest> depMap = new DependencyMap<>();

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
        String className = javaClass.getClassName();
        // just inform the predicate class
        WaitForJavaClassResult javaResult = predicate.apply(javaClass);
        ResultType result = javaResult.getResultType();
        if (ResultType.WAIT.equals(result))
        {
            // retry it later wait for YES/NO to push waiting to queue again
            waiting.put(request, getSender(), getSelf());
            depMap.put(request, javaResult.getClassForWait());
        }
        else
        {
            getSender().tell(new IsPersistenceResponse(request, ResultType.TRUE.equals(result)), getSelf());
            waiting.resend(depMap.getDependingOn(className));
        }
    }
}
