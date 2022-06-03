package com.intershop.tool.architecture.report.common.resources;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileRepository
{
    public void create(Path path, String fileName)
    {
        Path filePath = path.resolve(fileName);
        try
        {
            Files.createFile(filePath);
        }
        catch(IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
