/*
 * PipelineVisitor.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.java.model.pipelet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Represents a SAX handler that can parse Enfinity pipelines.
 */
public class PipeletHandler extends DefaultHandler
{
    private final PipeletDescriptor descriptor;
    private final String cartridgeName;
    private boolean isPipeletClass;
    private boolean isPipeletName;
    private String chars;

    /**
     * The constructor.
     * @param cartridgeName name of cartridge
     * @param descriptor pipelet information
     */
    public PipeletHandler(String cartridgeName, PipeletDescriptor descriptor)
    {
        this.descriptor = descriptor;
        this.cartridgeName = cartridgeName;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        isPipeletClass = "pipeletClass".equals(qName);
        isPipeletName = "pipeletName".equals(qName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (isPipeletClass)
        {
            descriptor.setPipeletClassName(chars);
            isPipeletClass = false;
        }
        if (isPipeletName)
        {
            descriptor.setReferenceName("enfinity:/" + cartridgeName + "/pipelets/" + chars + ".xml");
            isPipeletName = false;
        }
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] chars, int start, int length)
    {
        this.chars = String.valueOf(chars, start, length).trim();
    }
}