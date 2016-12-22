/*
 * XMLInputSourceVisitor.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A common superclass for all visitors that have to parse XML files.
 */
public class XMLInputSourceVisitor
{
    public void visitXMLInputSource(File file, DefaultHandler handler) throws SAXException, ParserConfigurationException, IOException
    {
        // parse XML
        SAXParserFactory pf = SAXParserFactory.newInstance();
        pf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        pf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        pf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        SAXParser p = pf.newSAXParser();

        // that's better because of parser error reporting
        p.parse(file, handler);
    }

    public void visitXMLInputSource(InputStream io, DefaultHandler handler) throws SAXException, ParserConfigurationException, IOException
    {
        // parse XML
        SAXParserFactory pf = SAXParserFactory.newInstance();
        pf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        pf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        pf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        SAXParser p = pf.newSAXParser();

        // that's better because of parser error reporting
        p.parse(io, handler);
    }
}
