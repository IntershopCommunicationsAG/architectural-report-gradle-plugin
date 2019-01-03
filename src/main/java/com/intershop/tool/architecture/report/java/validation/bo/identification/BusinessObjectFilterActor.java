package com.intershop.tool.architecture.report.java.validation.bo.identification;

import com.intershop.tool.architecture.report.common.actors.AbstractJavaClassActor;
import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;

public class BusinessObjectFilterActor extends AbstractJavaClassActor
{
    private final BusinessObjectPredicate predicate = new BusinessObjectPredicate();

    @Override
    protected void receive(JavaClassRequest request)
    {
        JavaClass javaClass = request.getJavaClass();
        ResultType result = predicate.apply(javaClass);
        if (!ResultType.WAIT.equals(result))
        {
            getSender().tell(new IsBusinessObjectResponse(request, ResultType.TRUE.equals(result)), getSelf());
        }
    }
}
