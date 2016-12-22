package com.intershop.tool.architecture.report.pipelet.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.java.validation.unused.ValidateUnusedResponse;

import akka.actor.UntypedActor;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business
 * objects only.
 */
public class PipeletGroupActor extends UntypedActor
{
    private static final Map<String, List<Issue>> issuesOfProject = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof ValidateUnusedResponse)
        {
            ValidateUnusedResponse request = (ValidateUnusedResponse)message;
            receive(request);
        }
        else if (AkkaMessage.TERMINATE.FLUSH_REQUEST.equals(message))
        {
            finish();
            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
        }
        else
        {
            unhandled(message);
        }
    }

    private static void receive(ValidateUnusedResponse request)
    {
        String projectName = request.getRequest().getProjectRef().getName();
        List<Issue> existingList = issuesOfProject.get(projectName);
        if (existingList == null)
        {
            issuesOfProject.put(projectName, new ArrayList<>(request.getIssues()));
        }
        else
        {
            existingList.addAll(request.getIssues());
        }
    }

    private void finish()
    {
        for(Entry<String, List<Issue>> entry : issuesOfProject.entrySet())
        {
            getSender().tell(new ValidateUnusedResponse(null, entry.getValue()), getSelf());
        }
    }
}
