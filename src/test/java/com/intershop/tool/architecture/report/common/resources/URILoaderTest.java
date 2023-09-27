package com.intershop.tool.architecture.report.common.resources;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class URILoaderTest
{
    private final static MockWebServer mockWebServer = new MockWebServer();

    private final static FileRepository fileRepository = new FileRepository();

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
    public void testInputStreamOfHttp() throws IOException, InterruptedException
    {
        InputStream inputStream = URILoader.getInputStream(mockWebServer.url("/").toString());
        assertInstanceOf(InputStream.class, inputStream);
    }

    @Test
    public void testInputStreamOfFile() throws IOException, InterruptedException
    {
        Path tempFile = Files.createTempFile("file", "txt");
        tempFile.toFile().deleteOnExit();

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
    public void testFileURICreationOnWindows() throws IOException
    {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.windows())) {
            // Create file in virtual file system
            String fileName = "file.txt";
            Path pathToVirtualStore = fileSystem.getPath("");

            fileRepository.create(pathToVirtualStore, fileName);
            Path filePath = pathToVirtualStore.resolve(fileName).toRealPath();
            assertTrue(Files.exists(filePath));

            // Create URI from string (Windows)
            URI uri = URILoader.createURIFromString(filePath.toString());
            assertNotNull(uri.getScheme());
            assertEquals("file", uri.getScheme());

            assertNotNull(uri.getPath());
            // C:\work is the default JimFS working directory
            String expectedPath = String.join(fileSystem.getSeparator(), "C:", "work", "file.txt");
            // Normalize URI path for assertion
            String uriPath = uri.getPath().replaceAll("[\\\\|/]", Matcher.quoteReplacement(fileSystem.getSeparator()));
            assertTrue(uriPath.endsWith(expectedPath), "URI path '" + uriPath + "' does not end with '" + expectedPath + "'");
        }
    }

    @Test
    public void testFileURICreationOnUnix() throws IOException
    {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            // Create file in virtual file system
            String fileName = "file.txt";
            Path pathToVirtualStore = fileSystem.getPath("");

            fileRepository.create(pathToVirtualStore, fileName);
            Path filePath = pathToVirtualStore.resolve(fileName).toRealPath();
            assertTrue(Files.exists(filePath));

            // Create URI from string (Unix)
            URI uri = URILoader.createURIFromString(filePath.toString());
            assertNotNull(uri.getScheme());
            assertEquals("file", uri.getScheme());

            assertNotNull(uri.getPath());
            // /work is the default JimFS working directory
            String expectedPath = "/work/file.txt";
            assertTrue(uri.getPath().endsWith(expectedPath), "URI path '" + uri.getPath() + "' does not end with '" + expectedPath + "'");
        }
    }

    @Test
    public void testHttpURICreation()
    {
        URI uri = URILoader.createURIFromString(mockWebServer.url("/").toString());
        assertEquals("http", uri.getScheme());
        assertEquals(mockWebServer.getHostName().toUpperCase(Locale.ROOT), uri.getHost().toUpperCase(Locale.ROOT)); // equalsIgnoreCase
        assertEquals(mockWebServer.getPort(), uri.getPort());
        assertEquals("/", uri.getPath());
        assertNull(uri.getFragment());
    }
}
