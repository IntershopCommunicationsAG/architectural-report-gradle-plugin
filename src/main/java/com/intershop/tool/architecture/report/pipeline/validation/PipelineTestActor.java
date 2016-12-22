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

import akka.actor.UntypedActor;

public class PipelineTestActor extends UntypedActor
{
    private static Set<String> EXISTING_START_NODES = new HashSet<>();
    private static Map<String, PipelineTestRequest> EXISTING_PIPELINE_REFS = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof PipelineTestRequest)
        {
            PipelineTestRequest request = (PipelineTestRequest)message;
            onReceive(request);
        }
        else if (AkkaMessage.TERMINATE.FLUSH_REQUEST.equals(message))
        {
            processNonExistingPipelineRefs();
            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
        }
        else
        {
            unhandled(message);
        }

    }

    private void processNonExistingPipelineRefs()
    {
        Map<String, PipelineTestRequest> result = new HashMap<>(EXISTING_PIPELINE_REFS);
        EXISTING_START_NODES.stream().forEach(s -> result.remove(s));
        List<Issue> issues = new ArrayList<>();
        for(Entry<String, PipelineTestRequest> entry : result.entrySet())
        {
            issues.add(new Issue(entry.getValue().getProjectRef(), ArchitectureReportConstants.KEY_INVALID_PIPELINEREF, entry.getValue().getPipeline().getName(), entry.getKey()));
        }
        getSender().tell(new NewIssuesResponse(issues), getSelf());

    }

    public void onReceive(PipelineTestRequest message)
    {
        collectStartNodes(message.getPipeline());
        collectPipelineRefs(message);
        getSender().tell(new PipelineTestResponse(message), getSelf());
    }

    private static void collectPipelineRefs(PipelineTestRequest message)
    {
        message.getPipeline().getPipelineRefs().stream().forEach(s -> EXISTING_PIPELINE_REFS.put(s, message));
    }

    private static void collectStartNodes(Pipeline pipeline)
    {
        pipeline.getStartNodes().stream().forEach(s -> EXISTING_START_NODES.add(pipeline.getName() + "-" + s));
    }

}
