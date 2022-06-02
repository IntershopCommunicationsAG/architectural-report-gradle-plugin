package com.intershop.tool.architecture.report.common.resources;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class URILoaderTest
{
    private final static MockWebServer mockWebServer = new MockWebServer();

    @BeforeAll
    public static void setUp() throws IOException
    {
        mockWebServer.setDispatcher(new Dispatcher()
        {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request)
            {
                if (Objects.equals(request.getPath(), "/"))
                {
                    return new MockResponse()
                                    .addHeader("Content-Type", "text/plain; charset=utf-8")
                                    .setBody("OK")
                                    .setResponseCode(200);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        mockWebServer.start(InetAddress.getLocalHost(), 0);
    }

    @AfterAll
    public static void tearDown() throws IOException
    {
        mockWebServer.shutdown();
    }

    @Test
    public void testInputStreamOfHttp() throws IOException
    {
        InputStream inputStream = URILoader.getInputStream(mockWebServer.url("/").toString());
        assertInstanceOf(InputStream.class, inputStream);
    }

    @Test
    public void testInputStreamOfFile(@TempDir Path tempDir) throws IOException
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
        String filePath = Paths.get("C:", "file.txt").toAbsolutePath().toString();
        URI uri = URILoader.createURIFromString(filePath);
        assertNotNull(uri.getScheme());
        assertEquals("file", uri.getScheme());

        assertNotNull(uri.getPath());
        assertTrue(uri.getPath().endsWith(filePath), "URI path '" + uri.getPath() + "' does not end with '" + filePath + "'");
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
