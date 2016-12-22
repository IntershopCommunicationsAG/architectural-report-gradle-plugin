package com.intershop.tool.architecture.report.isml.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class IsmlFinder implements Function<File, Collection<File>>
{
    @Override
    public Collection<File> apply(File templateFolder)
    {
        if (!templateFolder.exists() || !templateFolder.isDirectory())
        {
            return Collections.emptyList();
        }
        File[] files = templateFolder.listFiles();
        if (files == null || files.length == 0)
        {
            return Collections.emptyList();
        }
        ArrayList<File> result = new ArrayList<>();
        for(File file : files)
        {
            if (file.isFile() && file.getName().endsWith(".isml"))
            {
                result.add(file);
            }
            else if(file.isDirectory() && !file.getName().startsWith("."))
            {
                result.addAll(apply(file));
            }
        }
        return result;
    }

}
