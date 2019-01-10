package com.intershop.tool.architecture.report.java.model.jclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaHelper
{
    private static Map<String, String> nativeTypes = new HashMap<>();
    static
    {
        nativeTypes.put("V", "void");
        nativeTypes.put("Z", "boolean");
        nativeTypes.put("I", "int");
    }

    public static String getNormalizedClassName(String className)
    {
        className = className.replaceAll("/", ".");

        if (className.startsWith("L"))
        {
            // used for annotations
            className = className.substring(1);
        }

        int index = className.indexOf("[L");
        if (index >= 0)
        {
            className = className.substring(index + 2);
        }
        index = className.indexOf("+L");
        if (index >= 0)
        {
            className = className.substring(index + 2);
        }
        if (className.startsWith("ZL"))
        {
            className = className.substring(2);
        }
        if (className.startsWith("IL"))
        {
            className = className.substring(2);
        }
        if (className.startsWith("IIL"))
        {
            className = className.substring(3);
        }

        if (className.endsWith(";"))
        {
            className = className.substring(0, className.length() - 1);
        }

        while(className.endsWith("[]"))
        {
            className = className.substring(0, className.length() - 2);
        }
        String nativeType = nativeTypes.get(className);
        if (nativeType != null)
        {
            return nativeType;
        }
        return nativeType == null ? className :  nativeType;
    }

    /**
     * @param signature method signature of asm
     * @return normalized java class names of signature
     */
    public static Collection<String> splitSignature(String signature)
    {
        String[] parts = signature.split("[^/\\.\\w+]");
        Set<String> result = new HashSet<String>(parts.length);
        List<String> extendedTypes = new ArrayList<>();
        for (String className : parts)
        {
            if (!className.isEmpty() && !className.startsWith(".")
                            && (className.contains("/") || className.contains(".")))
            {
                String[] extendedPats = className.split("::?");
                if (extendedPats.length == 1)
                {
                    result.add(getNormalizedClassName(className));
                }
                else
                {
                    result.add(getNormalizedClassName(extendedPats[1]));
                    extendedTypes.add("T" + extendedPats[0]);
                }
            }
        }
        result.removeAll(extendedTypes);
        return result;
    }

    private static final String[] STANDARD_PACKAGES = { "java", "org.", "oracle.", "com.google.", "com.sun.", "junit.framework.", "junit.extensions", "ch.qos.logback.", "com.thoughtworks.", "com.sshtools." };

    /**
     * This function is necessary in a incomplete scenario, where some classes
     * are not available.
     * This method improves the performance of identification tasks (persistence, businessobject, pipelet), because "standard" classes can't be one of them.
     *
     * @param className class name including package
     * @return true in case the class is referenced by a standard package
     */
    public static boolean isStandardClass(String className)
    {
        for (String standardClass : STANDARD_PACKAGES)
        {
            if (className.startsWith(standardClass))
            {
                return true;
            }
        }
        return false;
    }
}
