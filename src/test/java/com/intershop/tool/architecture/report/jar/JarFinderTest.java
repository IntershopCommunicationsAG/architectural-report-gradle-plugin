package com.intershop.tool.architecture.report.jar;

import com.intershop.tool.architecture.report.java.model.jar.JarFinder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JarFinderTest
{
    private final JarFinder jarFinder = new JarFinder();

    @Test
    public void test() throws IOException
    {
        boolean found = false;
        Enumeration<URL> pathIts = getClass().getClassLoader().getResources("");
        while(pathIts.hasMoreElements())
        {
            URL path = pathIts.nextElement();
            File resourceFolder = new File(path.getFile());
            Collection<File> files = jarFinder.apply(resourceFolder);
            if (!files.isEmpty())
            {
                assertEquals(2, files.size(), "found four jars");
                assertTrue(files.stream().anyMatch(ref -> ref.getAbsolutePath().contains("test_ca")), "contains test_ca");
                found = true;
            }
        }
        assertTrue(found, "No jars found");
    }
}
