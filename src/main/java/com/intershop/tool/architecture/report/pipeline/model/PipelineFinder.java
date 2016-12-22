package com.intershop.tool.architecture.report.pipeline.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class PipelineFinder implements Function<File, Collection<File>>
{
    @Override
    public Collection<File> apply(File libFolder)
    {
        if (!libFolder.exists() || !libFolder.isDirectory())
        {
            return Collections.emptyList();
        }
        File[] files = libFolder.listFiles();
        if (files == null || files.length == 0)
        {
            return Collections.emptyList();
        }
        ArrayList<File> result = new ArrayList<>();
        for(File file : files)
        {
            if (file.isFile() && file.getName().endsWith(".pipeline"))
            {
                result.add(file);
            }
        }
        return result;
    }

}
