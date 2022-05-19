package com.intershop.tool.architecture.report.common.project;

public class DependencyListEntry
{
    private final Enum<DependencyListEntryType> type;
    private final String group;
    private final String module;
    private final String version;

    public DependencyListEntry(Enum<DependencyListEntryType> type, String group, String module, String version)
    {
        this.type = type;
        this.group = group;
        this.module = module;
        this.version = version;
    }

    public Enum<DependencyListEntryType> getType()
    {
        return type;
    }

    public String getGroup()
    {
        return group;
    }

    public String getModule()
    {
        return module;
    }

    public String getVersion()
    {
        return version;
    }
}
