package com.intershop.tool.architecture.report.java.model.pipelet;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.intershop.tool.architecture.report.common.resources.XMLInputSourceVisitor;

public class PipeletVisitor
{
    XMLInputSourceVisitor xmlVisitor = new XMLInputSourceVisitor();

    public PipeletDescriptor apply(String cartridgeName, File file)
    {
        PipeletDescriptor result = new PipeletDescriptor();
        PipeletHandler handler = new PipeletHandler(cartridgeName, result);
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
