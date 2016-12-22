package com.intershop.tool.architecture.report.cmd;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineArguments implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(CommandLineArguments.class);
    private final Map<String, String> arguments;

    public CommandLineArguments(Map<String, String> arguments)
    {
        this.arguments = Collections.unmodifiableMap(arguments);
    }

    public CommandLineArguments(String... args)
    {
        this(createArguments(args));
    }

    private static Map<String, String> createArguments(String[] args)
    {
        Map<String, String> result = new HashMap<>();
        String key = null;
        for (int i = 0; i < args.length; i++)
        {
            String parameter = args[i];
            if (parameter.startsWith("-"))
            {
                key = parameter.substring(1);
            }
            else if (key == null)
            {
                throw new IllegalArgumentException("Parameter doesn't starts with '-': '" + parameter + "'");
            }
            else
            {
                result.put(key, parameter);
                logger.info("Loaded configuration: '{}'='{}'", key, parameter);
            }
        }
        return result;
    }

    public Map<String, String> getArguments()
    {
        return arguments;
    }

    public String getArgument(String key)
    {
        return arguments.get(key);
    }
}
