package com.intershop.tool.architecture.report.java.model;

import com.intershop.tool.architecture.report.common.model.ResultType;

public class WaitForJavaClassResult
{
    private final ResultType resultType;
    private final String classForWait;

    public WaitForJavaClassResult(ResultType resultType, String classForWait)
    {
        this.resultType = resultType;
        this.classForWait = classForWait;
    }

    public ResultType getResultType()
    {
        return resultType;
    }

    public String getClassForWait()
    {
        return classForWait;
    }
};
