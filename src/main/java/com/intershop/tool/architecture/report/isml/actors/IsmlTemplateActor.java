package com.intershop.tool.architecture.report.isml.actors;

import java.io.File;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.isml.messages.IsmlValidationResponse;
import com.intershop.tool.architecture.report.isml.model.IsmlTemplateChecker;

import akka.actor.UntypedActor;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business
 * objects only.
 */
public class IsmlTemplateActor extends UntypedActor
{

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof FileRequest)
        {
            receive((FileRequest) message);
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

    private void receive(FileRequest request)
    {
        IsmlTemplateChecker checker = new IsmlTemplateChecker(request.getProjectRef(), new File(request.getFileName()));
        getSender().tell(new IsmlValidationResponse(request, checker.getIssues()), getSelf());
    }
}
