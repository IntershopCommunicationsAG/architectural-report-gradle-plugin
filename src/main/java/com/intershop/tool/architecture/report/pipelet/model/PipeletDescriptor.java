package com.intershop.tool.architecture.report.pipelet.model;

public class PipeletDescriptor
{
    private String referenceName;
    private String pipeletClassName;

    public PipeletDescriptor()
    {
    }

    public String getReferenceName()
    {
        return referenceName;
    }

    public void setReferenceName(String referenceName)
    {
        this.referenceName = referenceName;
    }

    public String getPipeletClassName()
    {
        return pipeletClassName;
    }

    public void setPipeletClassName(String pipeletClassName)
    {
        this.pipeletClassName = pipeletClassName;
    }
}
