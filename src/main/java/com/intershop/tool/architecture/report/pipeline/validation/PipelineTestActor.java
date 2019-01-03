package com.intershop.tool.architecture.report.pipeline.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.common.messages.NewIssuesResponse;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.pipeline.messages.PipelineTestRequest;
import com.intershop.tool.architecture.report.pipeline.messages.PipelineTestResponse;
import com.intershop.tool.architecture.report.pipeline.model.Pipeline;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

import akka.actor.AbstractActor;

public class PipelineTestActor extends AbstractActor
{
    private static Set<String> EXISTING_START_NODES = new HashSet<>();
    private static Map<String, ExistingPipelineRef> EXISTING_PIPELINE_REFS = new HashMap<>();

    private static class ExistingPipelineRef
    {
        private final String pipelineName;
        private final ProjectRef projectRef;

        private ExistingPipelineRef(ProjectRef projectRef, String pipelineName)
        {
            this.projectRef = projectRef;
            this.pipelineName = pipelineName;
        }
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(PipelineTestRequest.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            processNonExistingPipelineRefs();
                            EXISTING_START_NODES.clear();
                            EXISTING_PIPELINE_REFS.clear();
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
    }

    private void processNonExistingPipelineRefs()
    {
        Map<String, ExistingPipelineRef> result = new HashMap<>(EXISTING_PIPELINE_REFS);
        EXISTING_START_NODES.stream().forEach(s -> result.remove(s));
        List<Issue> issues = new ArrayList<>();
        for(Entry<String, ExistingPipelineRef> entry : result.entrySet())
        {
            issues.add(new Issue(entry.getValue().projectRef, ArchitectureReportConstants.KEY_INVALID_PIPELINEREF, entry.getValue().pipelineName, entry.getKey()));
        }
        getSender().tell(new NewIssuesResponse(issues), getSelf());
    }

    private void receive(PipelineTestRequest message)
    {
        collectStartNodes(message.getPipeline());
        collectPipelineRefs(message);
        getSender().tell(new PipelineTestResponse(message), getSelf());
    }

    private static void collectPipelineRefs(PipelineTestRequest message)
    {
        message.getPipeline().getPipelineRefs().stream().forEach(s -> {
            EXISTING_PIPELINE_REFS.put(s, new ExistingPipelineRef(message.getProjectRef(), message.getPipeline().getName()));
        });
    }

    private static void collectStartNodes(Pipeline pipeline)
    {
        pipeline.getStartNodes().stream().forEach(s -> EXISTING_START_NODES.add(pipeline.getName() + "-" + s));
    }

}
