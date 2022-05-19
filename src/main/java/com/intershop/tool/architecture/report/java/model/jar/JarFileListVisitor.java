package com.intershop.tool.architecture.report.java.model.jar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A JAR file list visitor that returns a collection of JAR files from given list file.
 */
public class JarFileListVisitor implements Function<File, Collection<File>>
{
    @Override
    public Collection<File> apply(File file)
    {
        try
        {
            if (!file.exists() || !file.isFile())
            {
                return Collections.emptyList();
            }
            // Read all lines (assuming we have relatively small text files)
            List<String> jarListLines = Files.readAllLines(file.toPath());

            if (jarListLines.isEmpty())
            {
                return Collections.emptyList();
            }

            Predicate<File> isJarFile = jarFile -> jarFile.exists() && jarFile.isFile() &&
                            jarFile.getName().endsWith(".jar");

            return jarListLines.stream()
                            .filter(line -> !line.isEmpty())
                            .map(File::new)
                            .filter(isJarFile)
                            .collect(Collectors.toList());
        }
        catch(IOException e)
        {
            throw new RuntimeException("Cannot read lines of file " + file.getAbsolutePath(), e);
        }
    }
}
