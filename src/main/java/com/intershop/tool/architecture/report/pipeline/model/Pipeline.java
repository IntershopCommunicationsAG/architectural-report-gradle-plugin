package com.intershop.tool.architecture.report.pipeline.model;

import java.util.ArrayList;
import java.util.List;

public class Pipeline
{
    private final String name;
    private List<String> pipeletRefs = new ArrayList<>();
    private List<String> pipelineRefs = new ArrayList<>();
    private List<String> startNodes = new ArrayList<>();

    public Pipeline(String name)
    {
        this.name = name;
    }

    public List<String> getPipeletRefs()
    {
        return pipeletRefs;
    }

    public void setPipeletRefs(List<String> pipeletRefs)
    {
        this.pipeletRefs = pipeletRefs;
    }

    public List<String> getPipelineRefs()
    {
        return pipelineRefs;
    }

    public void setPipelineRefs(List<String> pipelineRefs)
    {
        this.pipelineRefs = pipelineRefs;
    }

    public List<String> getStartNodes()
    {
        return startNodes;
    }

    public void setStartNodes(List<String> startNodes)
    {
        this.startNodes = startNodes;
    }

    public String getName()
    {
        return name;
    }

}
