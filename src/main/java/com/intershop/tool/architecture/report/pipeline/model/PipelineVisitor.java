package com.intershop.tool.architecture.report.pipeline.model;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.intershop.tool.architecture.report.common.XMLInputSourceVisitor;

public class PipelineVisitor implements Function<File, Pipeline>
{
    XMLInputSourceVisitor xmlVisitor = new XMLInputSourceVisitor();

    @Override
    public Pipeline apply(File file)
    {
        String name = file.getName();
        Pipeline result = new Pipeline(name.substring(0, name.length() - 9)); // .pipeline
        PipelineHandler handler = new PipelineHandler(result);
        try
        {
            xmlVisitor.visitXMLInputSource(file, handler);
        }
        catch(SAXException | ParserConfigurationException | IOException e)
        {
            throw new RuntimeException("Can't process files:" + file, e);
        }
        return result;
    }

}
