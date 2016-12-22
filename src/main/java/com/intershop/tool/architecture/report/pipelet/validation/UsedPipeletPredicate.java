package com.intershop.tool.architecture.report.pipelet.validation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

import com.intershop.tool.architecture.report.common.model.ResultType;
import com.intershop.tool.architecture.report.java.model.JavaClass;
import com.intershop.tool.architecture.report.pipelet.model.PipeletDescriptor;

/**
 * Stateful predicate, which returns three results: YES, NO and WAIT.
 * <ul>
 * <li>YES -class can be identified as persistent class</li>
 * <li>NO - class doesn't depend on persistent classes</li>
 * <li>WAIT - the predicate as not enough information about depending classes</li>
 * </ul>
 * A persistent class is a class, which depends on
 */
public class UsedPipeletPredicate implements Function<JavaClass, ResultType>
{
    /**
     * Contains pipelet references (e.g. "enfinity:/bc_costcenter/pipelets/AddBuyerToCostCenter.xml"), which couldn't be
     * resolved to a class name
     */
    private final Set<String> openRefs = new ConcurrentSkipListSet<>();

    /**
     * Contains pipelet class names, which are used in pipelines
     */
    private final Set<String> usedClasses = new ConcurrentSkipListSet<>();

    /**
     * Mapping between pipelet reference and pipelet class name
     */
    private final Map<String, String> refToClassName = new ConcurrentHashMap<>();
    private volatile boolean finished = false;

    public void registerUsedClassName(String className)
    {
        usedClasses.add(className);
    }

    /**
     * Register usage of pipelet (reference)
     *
     * @param pipeletRef
     *            pipelet reference inside of pipeline
     */
    public void registerUsage(String pipeletRef)
    {
        String className = refToClassName.get(pipeletRef);
        if (className == null)
        {
            openRefs.add(pipeletRef);
        }
        else
        {
            usedClasses.add(className);
        }
    }

    /**
     * Register pipelet mapping (reference to class name)
     * @param descriptor pipelet descriptor contains reference to class name mapping
     */
    public void registerMapping(PipeletDescriptor descriptor)
    {
        refToClassName.put(descriptor.getReferenceName(), descriptor.getPipeletClassName());
    }

    /**
     * @param javaClass pipelet class name
     * @return ResultType.TRUE in case the pipelet (class) is used
     */
    @Override
    public ResultType apply(JavaClass javaClass)
    {
        String className = javaClass.getClassName();
        if (usedClasses.contains(className))
        {
            return ResultType.TRUE;
        }
        if (isFinished())
        {
            return ResultType.FALSE;
        }
        return ResultType.WAIT;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public void finished()
    {
        for (String ref : openRefs)
        {
            registerUsage(ref);
        }
        finished = true;
    }
}
