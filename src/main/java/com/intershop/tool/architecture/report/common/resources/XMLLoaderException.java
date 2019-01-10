package com.intershop.tool.architecture.report.common.resources;

public class XMLLoaderException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public XMLLoaderException(String message, Exception e)
    {
        super(message, e);
    }
}
