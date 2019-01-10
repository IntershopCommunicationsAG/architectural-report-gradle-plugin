package com.intershop.tool.architecture.report.jar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import org.junit.Test;

import com.intershop.tool.architecture.report.java.model.jar.JarFinder;

public class JarFinderTest
{
    private JarFinder jarFinder = new JarFinder();

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
                assertEquals("found four jars", 2, files.size());
                assertTrue("contains test_ca", files.stream().filter(ref -> ref.getAbsolutePath().contains("test_ca")).findAny().isPresent());
                found = true;
            }
        }
        assertTrue("No jars found", found);
    }

}
