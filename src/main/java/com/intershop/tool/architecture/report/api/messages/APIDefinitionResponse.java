package com.intershop.tool.architecture.report.api.messages;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.common.model.Issue;

public class APIDefinitionResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Collection<Definition> collectedDefinitions;
    private final Collection<Definition> removedDefinitions;
    private final List<Issue> issues;

    public APIDefinitionResponse(Collection<Definition> collectedDefinitions, Collection<Definition> removedDefinitions, List<Issue> issues)
    {
        this.collectedDefinitions = collectedDefinitions;
        this.removedDefinitions = removedDefinitions;
        this.issues = issues;
    }

    public Collection<Definition> getRemovedDefinitions()
    {
        return removedDefinitions;
    }

    public Collection<Definition> getCollectedDefinitions()
    {
        return collectedDefinitions;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }

}
