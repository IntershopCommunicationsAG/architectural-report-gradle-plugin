package com.intershop.tool.architecture.report.java.validation.bo.identification;

import java.io.Serializable;

import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

public class IsBusinessObjectResponse implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final JavaClassRequest request;
    private final boolean isBusinessObject;

    public IsBusinessObjectResponse(JavaClassRequest request, boolean isBusinessObject)
    {
        this.request = request;
        this.isBusinessObject = isBusinessObject;
    }

    public boolean isBusinessObject()
    {
        return isBusinessObject;
    }

    public JavaClassRequest getRequest()
    {
        return request;
    }
}
