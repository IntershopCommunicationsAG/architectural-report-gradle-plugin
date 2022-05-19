package com.intershop.tool.architecture.report.common.issue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.intershop.tool.architecture.report.common.resources.XMLInputSourceVisitor;
import com.intershop.tool.architecture.report.common.resources.XMLLoaderException;

public class AzureIssuesVisitor implements Function<InputStream, List<AzureIssue>>
{
    XMLInputSourceVisitor xmlVisitor = new XMLInputSourceVisitor();

    @Override
    public List<AzureIssue> apply(InputStream io)
    {
        List<AzureIssue> result = new ArrayList<>();
        AzureIssuesHandler handler = new AzureIssuesHandler(result);
        try
        {
            xmlVisitor.visitXMLInputSource(io, handler);
        }
        catch(SAXException | ParserConfigurationException | IOException e)
        {
            throw new XMLLoaderException("Can't import existing issues", e);
        }
        return result;
    }

}
