package com.intershop.tool.architecture.report.jar;

import com.intershop.tool.architecture.report.java.model.jar.JarFileListVisitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JarFileListVisitorTest
{
    @TempDir
    static File tempDir;

    private static File JARS_TXT;

    private static final List<File> FILE_LIST = new ArrayList<>();

    private final JarFileListVisitor underTest = new JarFileListVisitor();

    @BeforeAll
    public static void setUp() throws IOException
    {
        tempDir.mkdirs();
        JARS_TXT = new File(tempDir, "current-jar-list.txt");

        FILE_LIST.add(new File(tempDir, "a.jar"));
        FILE_LIST.add(new File(tempDir, "b.jar"));
        FILE_LIST.add(new File(tempDir, "text.txt")); // Test non-jar file
        FILE_LIST.forEach(file -> {
            try
            {
                file.createNewFile();
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        });

        List<String> jarsLines = FILE_LIST.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        jarsLines.add(""); // Test empty line

        Files.write(JARS_TXT.toPath(), jarsLines);
    }

    @AfterAll
    public static void tearDown()
    {
        FILE_LIST.forEach(File::delete);
        JARS_TXT.delete();

        tempDir.delete();
    }

    @Test
    public void testExampleList()
    {
        Collection<File> jarFiles = underTest.apply(JARS_TXT);
        Assertions.assertEquals(2, jarFiles.size(), "Number of JARs");
    }
}