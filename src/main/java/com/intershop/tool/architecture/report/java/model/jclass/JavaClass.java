/*
 * JavaClass.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.java.model.jclass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.intershop.tool.architecture.report.api.model.definition.Definition;

/**
 * Represents a java class
 */
public class JavaClass implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String className;
    private final String superName;
    private final Set<String> usageRefs = new HashSet<>();
    private final Set<String> deprecatedRefs = new HashSet<>();
    private final Set<String> implementsRef = new HashSet<>();
    private final Set<String> extensionPointIDs = new HashSet<>();
    private boolean deprecated = false;
    private final List<Definition> apiDefinitions = new ArrayList<>();

    public JavaClass(String className, String superName)
    {
        this.className = className;
        this.superName = superName;
        usageRefs.add(superName);
        implementsRef.add(superName);
    }

    public String getClassName()
    {
        return className;
    }

    public Collection<String> getImplementsRef()
    {
        return implementsRef;
    }

    public Set<String> getExtensionPointIDs()
    {
        return extensionPointIDs;
    }

    public Collection<String> getUsageRefs()
    {
        return usageRefs;
    }

    public String getSuperName()
    {
        return superName;
    }

    public boolean isDeprecated()
    {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated)
    {
        this.deprecated = deprecated;
    }

    /**
     * @return list of classes, which are used in deprecated methods
     */
    public Collection<String> getDeprecatedRefs()
    {
        return deprecatedRefs;
    }

    public List<Definition> getApiDefinition()
    {
        return apiDefinitions;
    }

}
