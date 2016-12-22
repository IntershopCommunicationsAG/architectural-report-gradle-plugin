package com.intershop.tool.architecture.report.java.validation.po;

import java.io.Serializable;

import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

public class IsPersistenceResponse implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final JavaClassRequest request;
    private final boolean isPersistent;

    public IsPersistenceResponse(JavaClassRequest request, boolean isPersistent)
    {
        this.request = request;
        this.isPersistent = isPersistent;
    }

    public boolean isPersistent()
    {
        return isPersistent;
    }

    public JavaClassRequest getRequest()
    {
        return request;
    }
}