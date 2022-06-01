package com.intershop.tool.architecture.report.common.resources;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class URILoaderTest
{
    private final static MockWebServer mockWebServer = new MockWebServer();

    @TempDir
    private Path tempDir;

    @BeforeAll
    public static void setup() {
        mockWebServer.enqueue(new MockResponse()
                        .addHeader("Content-Type", "text/plain; charset=utf-8")
                        .setBody("OK")
                        .setResponseCode(200));
    }

    @Test
    public void testInputStreamOfHttp() throws IOException
    {
        InputStream inputStream = URILoader.getInputStream(mockWebServer.url("/").toString());
        assertInstanceOf(InputStream.class, inputStream);
    }

    @Test
    public void testInputStreamOfFile() throws IOException
    {
        Path tempFile = Files.createFile(tempDir.resolve("file.txt"));

        InputStream inputStream = URILoader.getInputStream(tempFile.toAbsolutePath().toString());
        assertInstanceOf(InputStream.class, inputStream);
    }

    @Test
    public void testInputStreamOfUnsupportedNews()
    {
        Exception exception = assertThrows(IOException.class, () -> URILoader.getInputStream("news:comp.lang.java"));

        String expectedMessage = "Scheme not support URI";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testFileURICreationOnWindows()
    {
        // Create URI from string (Windows)
        URI uri = URILoader.createURIFromString("C:\\file.txt");
        assertNotNull(uri.getScheme());
        assertEquals("file", uri.getScheme());

        String expectedPath = "C:\\file.txt";
        assertNotNull(uri.getPath());
        assertTrue(uri.getPath().endsWith(expectedPath), "URI path '" + uri.getPath() + "' does not end with '" + expectedPath + "'");
    }

    @Test
    public void testFileURICreationOnUnix()
    {
        // Create URI from string (Unix)
        URI uri = URILoader.createURIFromString("/file.txt");
        assertNotNull(uri.getScheme());
        assertEquals("file", uri.getScheme());

        String expectedPath = "/file.txt";
        assertNotNull(uri.getPath());
        assertTrue(uri.getPath().endsWith(expectedPath), "URI path '" + uri.getPath() + "' does not end with '" + expectedPath + "'");
    }

    @Test
    public void testHttpURICreation()
    {
        URI uri = URILoader.createURIFromString(mockWebServer.url("/").toString());
        assertEquals("http", uri.getScheme());
        assertEquals(mockWebServer.getHostName(), uri.getHost());
        assertEquals(mockWebServer.getPort(), uri.getPort());
        assertEquals("/", uri.getPath());
        assertNull(uri.getFragment());
    }
}
