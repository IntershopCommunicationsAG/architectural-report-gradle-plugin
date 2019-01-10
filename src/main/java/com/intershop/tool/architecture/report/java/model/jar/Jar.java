/*
 * Jar.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.java.model.jar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.model.pipelet.PipeletDescriptor;

/**
 * Represents a Jar file in a cartridge.
 */
public class Jar implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Collection<JavaClass> classes = new ArrayList<>();
    private Collection<PipeletDescriptor> pipeletDesciptor = new ArrayList<>();
    private final ProjectRef projectRef;

    public Jar(ProjectRef projectRef)
    {
        this.projectRef = projectRef;
    }

    public Collection<JavaClass> getClasses()
    {
        return classes;
    }

    public void add(JavaClass javaClass)
    {
        classes.add(javaClass);
    }

    public Collection<PipeletDescriptor> getPipeletDesciptor()
    {
        return pipeletDesciptor;
    }

    public void setPipeletDesciptor(Collection<PipeletDescriptor> pipeletDesciptor)
    {
        this.pipeletDesciptor = pipeletDesciptor;
    }

    public ProjectRef getProjectRef()
    {
        return projectRef;
    }

}
