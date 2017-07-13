package com.intershop.tool.architecture.report.api.messages;

import java.io.Serializable;
import java.util.Collection;

import com.intershop.tool.architecture.report.api.model.definition.Definition;

public class APIDefinitionRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Collection<Definition> definitions;

    public APIDefinitionRequest(Collection<Definition> definitions)
    {
        this.definitions = definitions;
    }

    public Collection<Definition> getDefinitions()
    {
        return definitions;
    }

}
