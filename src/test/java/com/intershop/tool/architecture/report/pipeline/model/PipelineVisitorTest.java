package com.intershop.tool.architecture.report.pipeline.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.intershop.tool.architecture.report.common.XMLInputSourceVisitor;

public class PipelineVisitorTest
{
    private static final String PIPELINE_NAME = "ViewCart";
    XMLInputSourceVisitor xmlVisitor = new XMLInputSourceVisitor();

    @Test
    public void test() throws SAXException, ParserConfigurationException, IOException
    {
        Pipeline result = new Pipeline(PIPELINE_NAME);
        PipelineHandler handler = new PipelineHandler(result);
        xmlVisitor.visitXMLInputSource(getClass().getResourceAsStream("/" + PIPELINE_NAME + ".pipeline"), handler);
        assertTrue(result.getStartNodes().contains("AddProduct"));
        assertEquals("count start nodes", 19, result.getStartNodes().size());
        // jump node reference
        assertTrue(result.getPipelineRefs().contains("ViewGiftCertificates-ValidateGCWebForm"));
        // dispatch reference
        assertTrue(result.getPipelineRefs().contains("ViewCart-DeletePromotion"));

        // direct references, but one JumpTarget could be calculated by resolving dispatch form action
        assertEquals("count pipeline refs", 83, result.getPipelineRefs().size());
    }

}
