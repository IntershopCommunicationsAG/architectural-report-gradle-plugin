package com.intershop.tool.architecture.report.pipelet.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.java.validation.unused.ValidateUnusedResponse;

import akka.actor.AbstractActor;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business
 * objects only.
 */
public class PipeletGroupActor extends AbstractActor
{
    private static final Map<String, List<Issue>> issuesOfProject = new HashMap<>();

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(ValidateUnusedResponse.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            finish();
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    private void receive(ValidateUnusedResponse request)
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
        issuesOfProject.clear();
    }
}
