package com.intershop.tool.architecture.report.pipeline.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.pipeline.model.Pipeline;

public class PipelineProcessor
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

    public List<Issue> collectIssues(ProjectRef projectRef)
    {
        Map<String, ExistingPipelineRef> result = new HashMap<>(EXISTING_PIPELINE_REFS);
        EXISTING_START_NODES.stream().forEach(s -> result.remove(s));
        List<Issue> issues = new ArrayList<>();
        for(Entry<String, ExistingPipelineRef> entry : result.entrySet())
        {
            if (projectRef.equals(entry.getValue().projectRef))
            {
                issues.add(new Issue(entry.getValue().projectRef, ArchitectureReportConstants.KEY_INVALID_PIPELINEREF, entry.getValue().pipelineName, entry.getKey()));
            }
        }
        return issues;
    }

    public void process(ProjectRef projectRef, Pipeline pipeline)
    {
        collectStartNodes(pipeline);
        collectPipelineRefs(projectRef, pipeline);
    }

    private static void collectPipelineRefs(ProjectRef projectRef, Pipeline pipeline)
    {
       pipeline.getPipelineRefs().stream().forEach(s -> {
            EXISTING_PIPELINE_REFS.put(s, new ExistingPipelineRef(projectRef, pipeline.getName()));
        });
    }

    private static void collectStartNodes(Pipeline pipeline)
    {
        pipeline.getStartNodes().stream().forEach(s -> EXISTING_START_NODES.add(pipeline.getName() + "-" + s));
    }

}
