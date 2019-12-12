package com.intershop.tool.architecture.report.java.model.jar;

public class JarParsingException extends RuntimeException
{
    private static final long serialVersionUID = -671512920527585557L;

    public JarParsingException(Throwable e)
    {
        super(e);
    }

    public JarParsingException(String message, Throwable e)
    {
        super(message, e);
    }
}
