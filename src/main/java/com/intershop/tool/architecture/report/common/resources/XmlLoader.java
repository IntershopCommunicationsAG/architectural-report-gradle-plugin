package com.intershop.tool.architecture.report.common.resources;

import java.io.InputStream;
import java.io.Writer;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class XmlLoader
{
    public void exportXML(Object xmlModel, Writer writer) throws JAXBException
    {
        // create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(xmlModel.getClass());
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(xmlModel, writer);
    }

    @SuppressWarnings("unchecked")
    public <T> T importXML(InputStream inputStream, Class<T> expectedType) throws XMLLoaderException
    {
        T definition = null;
        try
        {
            JAXBContext context = JAXBContext.newInstance(expectedType);
            Unmarshaller um = context.createUnmarshaller();
            definition = (T) um.unmarshal(inputStream);
        }
        catch(JAXBException e)
        {
            throw new XMLLoaderException("Can't load stream for class:" + expectedType.getName(), e);
        }
        return definition;
    }
}
