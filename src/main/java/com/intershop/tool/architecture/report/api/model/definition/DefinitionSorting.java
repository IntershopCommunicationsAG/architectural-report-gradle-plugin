package com.intershop.tool.architecture.report.api.model.definition;

import java.util.Comparator;

public class DefinitionSorting
{
    public static final Comparator<Definition> DEFINITION_COMPARATOR = (o1, o2) -> {
        int result = o1.getProjectRef().compareTo(o2.getProjectRef());
        if (result == 0)
        {
            result = o1.getArtifact().compareTo(o2.getArtifact());
        }
        if (result == 0)
        {
            result = o1.getSource().compareTo(o2.getSource());
        }
        if (result == 0)
        {
            result = o1.getSignature().compareTo(o2.getSignature());
        }
        return result;
    };
}
