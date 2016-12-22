/*
 * JavaRef.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.jar.messages;

import java.io.Serializable;

import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.jar.model.Jar;

/**
 * Represents a reference to a Java Archive.
 */
public class GetJarResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final FileRequest request;
    private final Jar jar;

    public GetJarResponse(FileRequest request, Jar jar)
    {
        this.request = request;
        this.jar = jar;
    }

    public FileRequest getRequest()
    {
        return request;
    }

    public Jar getJar()
    {
        return jar;
    }
}
