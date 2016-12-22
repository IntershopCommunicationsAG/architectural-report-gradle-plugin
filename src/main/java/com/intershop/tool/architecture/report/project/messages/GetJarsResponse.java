package com.intershop.tool.architecture.report.project.messages;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

public class GetJarsResponse
{
    private final GetJarsRequest request;
    private final Collection<String> fileNames;

    public GetJarsResponse(GetJarsRequest request, Collection<File> files)
    {
        this.request = request;
        this.fileNames = files.stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList());
    }

    public GetJarsRequest getRequest()
    {
        return request;
    }

    public Collection<String> getFileNames()
    {
        return fileNames;
    }
}
