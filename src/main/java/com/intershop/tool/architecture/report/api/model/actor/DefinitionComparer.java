package com.intershop.tool.architecture.report.api.model.actor;

import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.LibDefinitionMapper;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.versions.SemanticVersion;
import com.intershop.tool.architecture.versions.SemanticVersions;
import com.intershop.tool.architecture.versions.UpdateStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Collection of baseline {@link Definition}s are compared to potential altered list of definitions.
 * Used for new library detection or library SemVer {@link UpdateStrategy} violation as well as for altered API definitions.
 */
public class DefinitionComparer
{
    private static final Object API_SOURCE_DEPENDENCIES_TXT = LibDefinitionMapper.API_SOURCE_DEPENDENCIES_TXT;
    private final Collection<Definition> definitions;
    private final Collection<Definition> baseline;
    private final UpdateStrategy strategy;
    private final Collection<Definition> touchedDefinitions;
    private final String group;

    public DefinitionComparer(ProjectRef currentProject, Collection<Definition> definitions, Collection<Definition> baseline, UpdateStrategy strategy)
    {
        this.baseline = Collections.unmodifiableCollection(baseline);
        this.definitions = Collections.unmodifiableCollection(definitions);
        this.strategy = strategy;
        this.group = currentProject.getGroup();
        Set<Definition> apiBaseline = new HashSet<>(baseline);
        apiBaseline.removeAll(definitions);
        touchedDefinitions = apiBaseline.stream().filter(d -> !API_SOURCE_DEPENDENCIES_TXT.equals(d.getSource())).collect(Collectors.toList());
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
        if (UpdateStrategy.MAJOR == strategy)
        {
            return issues;
        }
        Map<String, String> libs = new HashMap<>();
        baseline.stream().filter(d -> API_SOURCE_DEPENDENCIES_TXT.equals(d.getSource()))
                        .forEach(d -> libs.put(getArtifact(d.getSignature()), getVersion(d.getSignature())));
        for (Definition d : definitions)
        {
            if (API_SOURCE_DEPENDENCIES_TXT.equals(d.getSource()) && !group.equals(getGroup(d.getSignature())))
            {
                String artifact = getArtifact(d.getSignature());
                String version = getVersion(d.getSignature());
                if (!libs.containsKey(artifact))
                {
                    issues.add(new Issue(d.getProjectRef(), ArchitectureReportConstants.KEY_NEW_LIBRARY, artifact));
                }
                else if (!isLibValid(artifact, libs.get(artifact), version))
                {
                    issues.add(new Issue(d.getProjectRef(), ArchitectureReportConstants.KEY_INVALID_LIBRARY, artifact + " update incompatible, was version " + libs.get(artifact)));
                }
            }
        }
        return issues;
    }

    private boolean isLibValid(String artifact, String oldVersion, String newVersion)
    {
        if (oldVersion.equals(newVersion))
        {
            return true;
        }
        return SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf(oldVersion), SemanticVersion.valueOf(newVersion), strategy);
    }

    private static String getVersion(String signature)
    {
        return signature.split("=")[1];
    }

    private static String getArtifact(String signature)
    {
        return signature.split("=")[0];
    }

    private static String getGroup(String signature)
    {
        return getArtifact(signature).split(":")[0];
    }

    private List<Issue> getAPIIssues()
    {
        Map<String, ProjectRef> touchedClasses = new HashMap<>();
        touchedDefinitions.forEach(d -> touchedClasses.put(d.getSource(), d.getProjectRef()));
        return touchedClasses.entrySet()
                        .stream().map(entry -> new Issue(entry.getValue(),
                                        ArchitectureReportConstants.KEY_JAVA_API_VIOLATION, entry.getKey()))
                        .collect(Collectors.toList());
    }

    public Collection<Definition> getAPIViolations()
    {
        return touchedDefinitions;
    }
}
