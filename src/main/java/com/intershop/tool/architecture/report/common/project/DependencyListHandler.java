package com.intershop.tool.architecture.report.common.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DependencyListHandler
{
    private ProjectRef project;
    private final Set<ProjectRef> projects = new HashSet<>();
    private final DependencyMatrix<ProjectRef, ProjectRef> projectDependencyMatrix = new DependencyMatrix<>();

    /**
     * Sets project and its dependencies based on text file and build a matrix
     * with a single project and its dependent projects.
     * Any text file line must follow such a structure:
     * <ul>
     *     <li>self:group:module:version</li>
     *     <li>library:group:module:version</li>
     *     <li>cartridge:group:module:version</li>
     * </ul>
     * The first line part represents the type of dependency.
     *
     * @param file Dependency list text file
     * @return DependencyListHandler
     */
    public DependencyListHandler parse(File file)
    {
        try
        {
            // Read all lines (assuming we have relatively small text files)
            List<String> textFileLines = Files.readAllLines(file.toPath());
            // Create supplier stream of parsed lines
            Supplier<Stream<DependencyListEntry>> dependencies = () -> textFileLines.stream()
                            .filter(line -> !line.isEmpty())
                            .map(this::parseLine);

            // Predicate for entry referencing current project (self)
            Predicate<DependencyListEntry> isEntryTypeSelf = entry -> entry.getType().equals(DependencyListEntryType.SELF);

            // Get current project based on predicate and set project
            DependencyListEntry currentProject = dependencies.get()
                            .filter(isEntryTypeSelf)
                            .findFirst()
                            .orElseThrow();
            setProject(currentProject);

            // Add all other dependencies to projects list
            dependencies.get().filter(isEntryTypeSelf.negate()).forEach(this::addToProjects);

            // Add current project and its dependencies to dependency matrix
            projectDependencyMatrix.addDependency(getProject(), getProjects());
        }
        catch(IOException e)
        {
            throw new RuntimeException("Can't process file:" + file, e);
        }
        catch(IllegalArgumentException e)
        {
            throw new RuntimeException("Can't determine dependency type in file:" + file, e);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            throw new RuntimeException("Can't determine malformed dependency in file:" + file, e);
        }
        catch(NoSuchElementException e)
        {
            throw new RuntimeException("Can't find project self reference in file:" + file, e);
        }

        return this;
    }

    /**
     * Parses line into four groups containing entry type, group, module and version.
     *
     * @throws IllegalArgumentException If entry type cannot be matched
     * @throws ArrayIndexOutOfBoundsException If not all dependency parts are present
     * @return New dependency list entry
     */
    private DependencyListEntry parseLine(String line)
    {
        String[] dependencyPart = line.split(":");

        DependencyListEntryType type = DependencyListEntryType.valueOf(dependencyPart[0].toUpperCase());
        String group = dependencyPart[1];
        // Handle also potential submodule names
        String module = String.join(":", Arrays.copyOfRange(dependencyPart, 2, dependencyPart.length - 1));
        String version = dependencyPart[dependencyPart.length - 1];

        return new DependencyListEntry(type, group, module, version);
    }

    public Collection<ProjectRef> getProjectDependencyMatrix()
    {
        return projectDependencyMatrix.getDependencies().get(getProject());
    }

    private void setProject(DependencyListEntry dependency)
    {
        project = createProject(dependency.getGroup(), dependency.getModule(), dependency.getVersion());
    }

    private void addToProjects(DependencyListEntry dependency)
    {
        projects.add(createProject(dependency.getGroup(), dependency.getModule(), dependency.getVersion()));
    }

    private static ProjectRef createProject(String org, String module, String version)
    {
        return new ProjectRef(org, module, version);
    }

    public ProjectRef getProject()
    {
        return project;
    }

    public Set<ProjectRef> getProjects()
    {
        return projects;
    }
}
