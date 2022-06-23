package com.intershop.tool.architecture.report.pipeline.model;

import com.intershop.tool.architecture.report.common.resources.XMLInputSourceVisitor;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(19, result.getStartNodes().size(), "count start nodes");
        // jump node reference
        assertTrue(result.getPipelineRefs().contains("ViewGiftCertificates-ValidateGCWebForm"));
        // dispatch reference
        assertTrue(result.getPipelineRefs().contains("ViewCart-DeletePromotion"));

        // direct references, but one JumpTarget could be calculated by resolving dispatch form action
        assertEquals(83, result.getPipelineRefs().size(), "count pipeline refs");
    }
}
