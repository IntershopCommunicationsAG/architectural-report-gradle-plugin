package com.intershop.tool.architecture.report.isml.actors;

import java.io.File;

import com.intershop.tool.architecture.report.common.actors.AbstractFileActor;
import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.isml.messages.IsmlValidationResponse;
import com.intershop.tool.architecture.report.isml.model.IsmlTemplateChecker;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business
 * objects only.
 */
public class IsmlTemplateActor extends AbstractFileActor
{
    @Override
    protected void receive(FileRequest request)
    {
        IsmlTemplateChecker checker = new IsmlTemplateChecker(request.getProjectRef(), new File(request.getFileName()));
        getSender().tell(new IsmlValidationResponse(request, checker.getIssues()), getSelf());
    }
}
