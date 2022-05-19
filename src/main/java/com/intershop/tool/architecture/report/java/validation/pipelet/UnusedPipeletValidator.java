package com.intershop.tool.architecture.report.java.validation.pipelet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jar.Jar;
import com.intershop.tool.architecture.report.pipeline.model.Pipeline;

/**
 * BusinessObjectValidatorActor receives validation events for business objects.
 * The received messages contains business objects only.
 */
public class UnusedPipeletValidator
{
    private final List<String> usedPipelets = new ArrayList<>();
    private final Map<ProjectRef, List<String>> mapRegisteredPipelets = new HashMap<>();

    public void process(Pipeline pipeline)
    {
        usedPipelets.addAll(pipeline.getPipeletRefs());
    }

    public void process(ProjectRef projectRef, Jar jar)
    {
        List<String> registeredPipelets = mapRegisteredPipelets.get(projectRef);
        if (registeredPipelets == null)
        {
            registeredPipelets = new ArrayList<>();
            mapRegisteredPipelets.put(projectRef, registeredPipelets);
        }
        final List<String> finalPipelets = registeredPipelets;
        jar.getPipeletDescriptor().stream().forEach(d -> finalPipelets.add(d.getPipeletClassName()));
    }
}
