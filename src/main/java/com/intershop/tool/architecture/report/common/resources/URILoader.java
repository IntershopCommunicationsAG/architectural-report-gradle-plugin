package com.intershop.tool.architecture.report.common.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URILoader
{
    private static final Logger logger = LoggerFactory.getLogger(URILoader.class);

    public static InputStream getInputStream(URI uri) throws IOException, InterruptedException
    {
        if (uri.getScheme().startsWith("http"))
        {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .version(HttpClient.Version.HTTP_2)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
            return getInputStream(request);
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

    private static InputStream getInputStream(HttpRequest request) throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
        if (response.statusCode() == 200)
        {
            logger.info("URI loaded: '{}'", request.uri());
            return response.body();
        }
        else
        {
            throw new IOException("Can't get response from URI '" + request.uri() + "' status=" +
                                  response.statusCode() + ".");
        }
    }

    public static InputStream getInputStream(String location) throws IOException, InterruptedException
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
    public static URI createURIFromString(String location)
    {
        URI uri;
        try
        {
            // Create URI by parsing location
            uri = URI.create(location);

            // Check if required scheme is available
            if (uri.getScheme() == null)
            {
                throw new IllegalArgumentException();
            }
        }
        catch(IllegalArgumentException e)
        {
            // Fallback to try as file
            uri = new File(location).toURI();
        }

        return uri;
    }
}
