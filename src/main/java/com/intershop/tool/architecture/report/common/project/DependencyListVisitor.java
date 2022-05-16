package com.intershop.tool.architecture.report.common.project;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;

public class DependencyListVisitor implements Function<File, Collection<ProjectRef>>
{
    /**
     * @param file Dependency list file
     * @return A matrix with a single project and multiple dependent projects
     */
    @Override
    public Collection<ProjectRef> apply(File file)
    {
        DependencyListHandler handler = new DependencyListHandler().parse(file);

        return handler.getProjectDependencyMatrix();
    }
}
