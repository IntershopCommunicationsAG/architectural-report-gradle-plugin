package com.intershop.tool.architecture.report.common.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class ResourceLoader
{
    public static String getString(String resource) throws IOException
    {
        StringWriter writer = new StringWriter();
        IOUtils.copy(getInputStream(resource), writer, "UTF-8");
        return writer.toString();
    }

    public static InputStream getInputStream(String resource) throws IOException
    {
        InputStream resourceAsStream = ResourceLoader.class.getClassLoader().getResourceAsStream(resource);
        if (resourceAsStream == null)
        {
            throw new FileNotFoundException("Can't find resource: " + resource);
        }
        return resourceAsStream;
    }
}
