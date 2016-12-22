package com.intershop.tool.architecture.report.isml.messages;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

public class GetIsmlTemplatesResponse
{
    private final GetIsmlTemplatesRequest request;
    private final Collection<String> fileNames;

    public GetIsmlTemplatesResponse(GetIsmlTemplatesRequest request, Collection<File> files)
    {
        this.request = request;
        this.fileNames = files.stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList());
    }

    public GetIsmlTemplatesRequest getRequest()
    {
        return request;
    }

    public Collection<String> getFileNames()
    {
        return fileNames;
    }

}
