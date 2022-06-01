package com.intershop.tool.architecture.report.common.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URILoader
{
    private static final Logger logger = LoggerFactory.getLogger(URILoader.class);

    public static InputStream getInputStream(URI uri) throws IOException
    {
        if (uri.getScheme().startsWith("http"))
        {
            return getInputStream(ClientBuilder.newClient().target(uri));
        }
        else if (uri.getScheme().startsWith("file"))
        {
            File file = new File(uri.toURL().getFile());
            return new FileInputStream(file);
        }
        else
        {
            throw new IOException("Scheme not support URI '" + uri + "'.");
        }
    }

    private static InputStream getInputStream(WebTarget webTarget) throws IOException
    {
        Response response = webTarget.request().get();
        if (Status.OK.getStatusCode() == response.getStatus())
        {
            logger.info("URI loaded: '{}'", webTarget.getUri().toString());
            return (InputStream)response.getEntity();
        }
        else
        {
            throw new IOException("Can't get response from URI '" + webTarget.getUri().toString() + "' status="
                            + response.getStatus() + ".");
        }
    }

    public static InputStream getInputStream(String location) throws IOException
    {
        return getInputStream(createURIFromString(location));
    }

    /**
     * Creates a URI by parsing the location string and fallback
     * to a file URI if the scheme component was not detected by {@link URI#create(String)}.
     *
     * @param location The string to be parsed into a UR
     * @return New URI
     */
    public static URI createURIFromString(String location) {
        URI uri;
        try {
            // Create URI by parsing location
            uri = URI.create(location);

            // Check if required scheme is available
            if (uri.getScheme() == null)
            {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            // Fallback to try as file
            uri = new File(location).toURI();
        }

        return uri;
    }
}
