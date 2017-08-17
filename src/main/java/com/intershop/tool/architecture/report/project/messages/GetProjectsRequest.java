package com.intershop.tool.architecture.report.project.messages;

public class GetProjectsRequest
{
    private final String ivyFile;

    public GetProjectsRequest(String ivyFile)
    {
        this.ivyFile = ivyFile;
    }

    public String getIvyFile()
    {
        return ivyFile;
    }
}
