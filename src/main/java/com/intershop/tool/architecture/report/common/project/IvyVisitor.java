package com.intershop.tool.architecture.report.common.project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.intershop.tool.architecture.report.common.resources.XMLInputSourceVisitor;

public class IvyVisitor implements Function<File, Collection<ProjectRef>>
{
    XMLInputSourceVisitor xmlVisitor = new XMLInputSourceVisitor();

    /**
     * @param file ivy file location
     * @return a matrix with a single project and multiple dependent projects
     */
    @Override
    public Collection<ProjectRef> apply(File file)
    {
        IvyHandler handler = new IvyHandler();
        DependencyMatrix<ProjectRef, ProjectRef> result = new DependencyMatrix<>();
        try
        {
            xmlVisitor.visitXMLInputSource(file, handler);
            result.addDependency(handler.getProject(), handler.getProjects());
        }
        catch(SAXException | ParserConfigurationException | IOException e)
        {
            throw new RuntimeException("Can't process files:" + file, e);
        }
        return result.getDependencies().get(handler.getProject());
    }

}
