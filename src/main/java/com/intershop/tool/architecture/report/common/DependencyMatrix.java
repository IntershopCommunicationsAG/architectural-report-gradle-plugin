/*
 * DependencyMatrix.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A matrix containing the dependency counts between groups of elements. The
 * elements can be anything, like Java classes, pipelines etc. Groups can be
 * containers of elements, like Java packages or cartridges.
 */
public class DependencyMatrix<F, T>
{
    private Map<F, Set<T>> dependencies = new HashMap<>();

    /**
     * Adds a dependency to the matrix. The count will be accumulated.
     * @param from source of dependency
     * @param to target of dependency
     */
    public void addDependency(F from, T to)
    {
        if (!from.equals(to))
        {
            Set<T> usedGroups = dependencies.get(from);
            if (usedGroups == null)
            {
                usedGroups = new HashSet<>();
                dependencies.put(from, usedGroups);
            }
            usedGroups.add(to);
        }
    }

    public Map<F, Set<T>> getDependencies()
    {
        return dependencies;
    }

    public void addDependency(F from, Collection<T> toCollection)
    {
        for (T to : toCollection)
        {
            addDependency(from, to);
        }
    }

}
