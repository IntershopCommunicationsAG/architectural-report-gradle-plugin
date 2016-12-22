package com.intershop.tool.architecture.report.java.model;

import java.io.Serializable;

import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class JavaClassRequest implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final JavaClass javaClass;
    // useful reference cartridge to collect problems of one project
    private final ProjectRef projectRef;

    public JavaClassRequest(JavaClass javaClass, ProjectRef projectRef)
    {
        this.javaClass = javaClass;
        this.projectRef = projectRef;
    }

    public JavaClass getJavaClass()
    {
        return javaClass;
    }

    public ProjectRef getProjectRef()
    {
        return projectRef;
    }
}
