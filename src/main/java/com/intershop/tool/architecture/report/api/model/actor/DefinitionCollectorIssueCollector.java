package com.intershop.tool.architecture.report.api.model.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.project.model.LibDefinitionMapper;
import com.intershop.tool.architecture.report.project.model.ProjectRef;
import com.intershop.tool.architecture.versions.SemanticVersion;
import com.intershop.tool.architecture.versions.SemanticVersions;
import com.intershop.tool.architecture.versions.UpdateStrategy;

/**
 * BusinessObjectValidatorActor receives validation events for business objects.
 * The received messages contains business objects only.
 */
public class DefinitionCollectorIssueCollector
{
    private static final Object API_SOURCE_IVY_XML = LibDefinitionMapper.API_SOURCE_IVY_XML;
    private final Collection<Definition> definitions;
    private final Collection<Definition> baseline;

    public DefinitionCollectorIssueCollector(Collection<Definition> definitions, Collection<Definition> baseline)
    {
        this.baseline = Collections.unmodifiableCollection(baseline);
        this.definitions = Collections.unmodifiableCollection(definitions);
    }

    public List<Issue> getIssues()
    {
        List<Issue> issues = getLibIssues();
        issues.addAll(getAPIIssues());
        return issues;
    }

    private List<Issue> getLibIssues()
    {
        List<Issue> issues = new ArrayList<>();
        Map<String, String> libs = new HashMap<>();
        baseline.stream().filter(d -> API_SOURCE_IVY_XML.equals(d.getSource()))
                        .forEach(d -> libs.put(getArtifact(d.getSignature()), getVersion(d.getSignature())));
        for (Definition d : definitions)
        {
            if (API_SOURCE_IVY_XML.equals(d.getSource()))
            {
                String artifact = getArtifact(d.getSignature());
                String version = getVersion(d.getSignature());
                if (!libs.containsKey(artifact))
                {
                    issues.add(new Issue(d.getProjectRef(), ArchitectureReportConstants.KEY_NEW_LIBRARY, d.getSignature()));
                }
                else if (!isLibValid(artifact, libs.get(artifact), version))
                {
                    issues.add(new Issue(d.getProjectRef(), ArchitectureReportConstants.KEY_INVALID_LIBRARY, d.getSignature() + " but was " + libs.get(artifact)));
                }
            }
        }
        return issues;
    }

    private static boolean isLibValid(String artifact, String oldVersion, String newVersion)
    {
        if (oldVersion.equals(newVersion))
        {
            return true;
        }
        return SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf(oldVersion), SemanticVersion.valueOf(newVersion), UpdateStrategy.MINOR);
    }

    private static String getVersion(String signature)
    {
        return signature.split("=")[1];
    }

    private static String getArtifact(String signature)
    {
        return signature.split("=")[0];
    }

    private List<Issue> getAPIIssues()
    {
        Set<Definition> apiBaseline = new HashSet<>(baseline);
        apiBaseline.removeAll(definitions);
        Map<String, ProjectRef> touchedClasses = new HashMap<>();
        apiBaseline.stream().filter(d -> !API_SOURCE_IVY_XML.equals(d.getSource())).forEach(d -> touchedClasses.put(d.getSource(), d.getProjectRef()));
        return touchedClasses.entrySet()
                        .stream().map(entry -> new Issue(entry.getValue(),
                                        ArchitectureReportConstants.KEY_API_VIOLATION, entry.getKey()))
                        .collect(Collectors.toList());
    }
}
