package com.intershop.tool.architecture.report.java.validation.pipelet;

import java.io.Serializable;

import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

public class IsPipeletResponse implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final JavaClassRequest request;
    private final boolean isPipelet;

    public IsPipeletResponse(JavaClassRequest request, boolean isPipelet)
    {
        this.request = request;
        this.isPipelet = isPipelet;
    }

    public JavaClassRequest getRequest()
    {
        return request;
    }

    public boolean isPipelet()
    {
        return isPipelet;
    }
}
