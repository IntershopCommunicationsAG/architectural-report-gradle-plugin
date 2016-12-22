package com.intershop.tool.architecture.report.isml.model;

public class FilePosition
{
    private final long line;
    private final int column;

    public FilePosition(long line, int column)
    {
        this.line = line;
        this.column = column;
    }

    public long getLine()
    {
        return line;
    }

    public int getColumn()
    {
        return column;
    }
}
