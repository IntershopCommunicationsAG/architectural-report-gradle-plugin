package com.intershop.tool.architecture.report.common.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyMap<T>
{
    private final Map<String, List<T>> pendingOn = new HashMap<>();

    public void put(T message, String dependsOn)
    {
        if (contains(message, dependsOn))
        {
            throw new IllegalArgumentException("Cycle between classes found" + message + ":" + dependsOn);
        }
        add(message, dependsOn);
    }

    private void add(T message, String dependsOn)
    {
        List<T> messages = pendingOn.get(dependsOn);
        if (messages == null)
        {
            messages = new ArrayList<>();
            pendingOn.put(dependsOn, messages);
        }
        messages.add(message);
    }

    private boolean contains(T message, String dependsOn)
    {
        List<T> depending = pendingOn.get(message);
        return depending == null ? false : depending.contains(dependsOn);
    }

    public List<T> getDependingOn(String dependsOn)
    {
        List<T> result = pendingOn.get(dependsOn);
        return result == null ? Collections.emptyList() : Collections.unmodifiableList(result);
    }
}
