package com.intershop.tool.architecture.report.java.validation.capi;

import java.util.function.Function;

import com.intershop.tool.architecture.report.common.actors.AbstractJavaClassActor;
import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.common.model.ValidationResult;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

/**
 * BusinessObjectValidatorActor receives validation events for business objects. The received messages contains business objects only.
 */
public class CapiValidatorActor extends AbstractJavaClassActor
{
    private final Function<JavaClassRequest, ValidationResult> validator = new CapiUsingInternalValidator();

    @Override
    protected void receive(JavaClassRequest request)
    {
        ValidationResult result = validator.apply(request);
        if (ResultType.TRUE.equals(result.getResultType()))
        {
            getSender().tell(new ValidateCapiResponse(request, result.getIssues()), getSelf());
        }
    }
}
