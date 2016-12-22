/*
 * JavaRef.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.common.messages;

import java.io.Serializable;

import com.intershop.tool.architecture.report.project.model.ProjectRef;

/**
 * Represents a reference to a Java Archive.
 */
public class FileRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final ProjectRef projectRef;

    public FileRequest(String fileName, ProjectRef projectRef)
    {
        this.projectRef = projectRef;
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public ProjectRef getProjectRef()
    {
        return projectRef;
    }

}
