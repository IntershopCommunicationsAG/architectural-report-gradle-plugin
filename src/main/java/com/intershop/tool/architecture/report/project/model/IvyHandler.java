/*
 * IvyVisitor.java
 *
 * Copyright (c) 2012 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.project.model;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Represents a SAX handler that can parse Ivy descriptor files.
 */
public class IvyHandler extends DefaultHandler
{
    /*
     * <?xml version="1.0" encoding="UTF-8"?>
     * <ivy-module
     *     <info organisation="com.intershop"  module="core" revision="7.3.2.0.188">
     *     </info>
     *     <dependencies>
     *         <dependency org="com.intershop" name="pf_cartridge" rev="${version.number}.+"/>
     *         <dependency org="com.intershop" name="pf_objectgraph" rev="${version.number}.+"/>
     *         <dependency org="com.intershop" name="pf_objectgraph_guice" rev="${version.number}.+"/>
     *         <dependency org="com.intershop" name="pf_extension" rev="${version.number}.+"/>
     *     </dependencies>
     * </ivy-module>
     */
    private final Set<ProjectRef> projects = new HashSet<>();
    private ProjectRef project = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if ("dependency".equals(qName))
        {
            projects.add(createProject(attributes.getValue("org"), attributes.getValue("name"),
                            attributes.getValue("rev")));
        }
        else if ("info".equals(qName))
        {
            project = createProject(attributes.getValue("organisation"), attributes.getValue("module"),
                            attributes.getValue("revision"));
        }
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
