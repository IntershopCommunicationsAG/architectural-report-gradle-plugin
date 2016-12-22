/*
 * PipelineVisitor.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.pipeline.model;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Represents a SAX handler that can parse Enfinity pipelines.
 */
public class PipelineHandler extends DefaultHandler
{
    private final Pipeline pipeline;
    private boolean isInPipelet = false;
    private boolean isDispatchFormAction = false;
    private Set<String> collectedPipelineRefs = new HashSet<>();

    /**
     * The constructor.
     * @param pipeline pipeline object will be filled by this handler
     */
    public PipelineHandler(Pipeline pipeline)
    {
        this.pipeline = pipeline;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        super.endElement(uri, localName, qName);
        // <pipelet href="enfinity:/bc_foundation/pipelets/KeyMapper.xml"/>
        if ("pipelet".equals(qName))
        {
            pipeline.getPipeletRefs().add(attributes.getValue("href"));
            if (attributes.getValue("href").contains("DispatchFormAction"))
            {
                isDispatchFormAction = true;
            }
        }
        // <startNode referencedName="ProcessGiftCard-IsDigitalCertificate"/>
        else if ("startNode".equals(qName))
        {
            String pipelineName = attributes.getValue("referencedName");
            // <startNode referencedName="ViewCart-View"/>
            // <startNode objectPath="JumpTarget"/>
            if (pipelineName != null && !pipelineName.isEmpty())
            {
                pipeline.getPipelineRefs().add(getPipelineRef(pipelineName));
            }
        }
        // <nodes xsi:type="pipeline:StartNode" nodeID="StartNode5" name="AddProduct">
        else if ("nodes".equals(qName))
        {
            if ("pipeline:StartNode".equals(attributes.getValue("xsi:type")))
            {
                pipeline.getStartNodes().add(attributes.getValue("name"));
            }
            else if ("pipeline:PipeletNode".equals(attributes.getValue("xsi:type")))
            {
                isInPipelet = true;
            }
        }
        // <configurationValues name="OutValue_0" value="ViewCart-View"/>
        else if ("configurationValues".equals(qName))
        {
            if (attributes.getValue("name").startsWith("OutValue_"))
            {
                collectedPipelineRefs.add(attributes.getValue("value"));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        super.endElement(uri, localName, qName);
        if ("nodes".equals(qName))
        {
            /*
             * <nodes xsi:type="pipeline:PipeletNode" nodeID="DispatchFormAction0">
             * <configurationValues name="OutValue_0" value="ViewCart-View"/>
             * <pipelet href="enfinity:/bc_foundation/pipelets/DispatchFormAction.xml"/>
             * </nodes>
             */
            if (isInPipelet && isDispatchFormAction)
            {
                for (String value : collectedPipelineRefs)
                {
                    pipeline.getPipelineRefs().add(getPipelineRef(value));
                }
            }
            isInPipelet = false;
            isDispatchFormAction = false;
            collectedPipelineRefs.clear();
        }
    }

    private String getPipelineRef(String value)
    {
        if (value.startsWith("This:"))
        {
            return pipeline.getName() + "-" + value.substring(5);
        }
        else if (value.startsWith("Super:"))
        {
            return pipeline.getName() + "-" + value.substring(6);
        }
        return value;
    }

}
