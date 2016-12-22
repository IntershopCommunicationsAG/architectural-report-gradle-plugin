package com.intershop.tool.architecture.report.pipeline.messages;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

public class GetPipelinesResponse
{
    private final GetPipelinesRequest request;
    private final Collection<String> fileNames;

    public GetPipelinesResponse(GetPipelinesRequest request, Collection<File> files)
    {
        this.request = request;
        this.fileNames = files.stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList());
    }

    public GetPipelinesRequest getRequest()
    {
        return request;
    }

    public Collection<String> getFileNames()
    {
        return fileNames;
    }
}
