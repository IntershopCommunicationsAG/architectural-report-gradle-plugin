package com.intershop.tool.architecture.report.pipelet.actors;

import java.util.Arrays;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.akka.actors.tooling.AkkaWaitingMessages;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.jar.messages.GetJarResponse;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;
import com.intershop.tool.architecture.report.java.validation.unused.ValidateUnusedResponse;
import com.intershop.tool.architecture.report.pipelet.validation.UsedPipeletPredicate;
import com.intershop.tool.architecture.report.pipeline.messages.PipelineResponse;

import akka.actor.AbstractActor;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business
 * objects only.
 */
public class UnusedPipeletValidatorActor extends AbstractActor
{
    private static final UsedPipeletPredicate PREDICATE = new UsedPipeletPredicate();
    private static AkkaWaitingMessages<JavaClassRequest> waiting = new AkkaWaitingMessages<>();

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(GetJarResponse.class, this::receive)
                        .match(JavaClassRequest.class, this::receive)
                        .match(PipelineResponse.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            PREDICATE.finished();
                            waiting.resend();
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    private void receive(PipelineResponse request)
    {
        request.getPipeline().getPipeletRefs().stream().forEach(r -> PREDICATE.registerUsage(r));
    }

    private void receive(GetJarResponse request)
    {
        request.getJar().getPipeletDesciptor().stream().forEach(d -> PREDICATE.registerMapping(d));
    }

    private void receive(JavaClassRequest request)
    {
        JavaClass javaClass = request.getJavaClass();
        for (String used : javaClass.getUsageRefs())
        {
            PREDICATE.registerUsedClassName(used);
        }
        ResultType result = PREDICATE.apply(request.getJavaClass());
        if (ResultType.FALSE.equals(result))
        {
            // not used
            getSender().tell(createResponse(ArchitectureReportConstants.KEY_PIPELET_UNUSED, request, javaClass), getSelf());
        }
        else if (ResultType.WAIT.equals(result))
        {
            waiting.put(request, getSender(), getSelf());
        }
        else if (ResultType.TRUE.equals(result))
        {
            if (javaClass.isDeprecated())
            {
                getSender().tell(createResponse(ArchitectureReportConstants.KEY_PIPELET_USED_DEPRECATED, request, javaClass), getSelf());
            }
        }
    }

    private static ValidateUnusedResponse createResponse(String key, JavaClassRequest request, JavaClass javaClass)
    {
        return new ValidateUnusedResponse(request, Arrays.asList(new Issue(request.getProjectRef(), key, javaClass.getClassName())));
    }

}
