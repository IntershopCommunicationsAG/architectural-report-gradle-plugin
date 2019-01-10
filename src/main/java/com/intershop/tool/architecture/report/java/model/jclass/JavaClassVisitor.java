package com.intershop.tool.architecture.report.java.model.jclass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.intershop.tool.architecture.report.api.model.definition.Artifact;
import com.intershop.tool.architecture.report.api.model.definition.Definition;

/**
 * Represents a Java class visitor that can parse java classes.
 */
public class JavaClassVisitor extends ClassVisitor
{
    private static final int API_VERSION = Opcodes.ASM5;
    private final JavaClass javaClass;
    private String currentClassName = null;

    /**
     * The constructor.
     * @param javaClass java class information will be filled by this handler
     */
    public JavaClassVisitor(JavaClass javaClass)
    {
        super(API_VERSION);
        this.javaClass = javaClass;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        // collect all dependencies that this class has, but only once!
        // afterwards, add them into the main matrix.
        currentClassName = getNormalizedClassName(name);

        if (isAcceptable(currentClassName))
        {
            for (String intf : interfaces)
            {
                addInterfaceName(getNormalizedClassName(intf));
            }

            javaClass.setDeprecated(isDeprecated(access));
        }
    }

    private static boolean isDeprecated(int access)
    {
        return (access & Opcodes.ACC_DEPRECATED) != 0;
    }

    private static boolean isPublic(int access)
    {
        return (access & Opcodes.ACC_PUBLIC) != 0;
    }

    private static boolean isProtected(int access)
    {
        return (access & Opcodes.ACC_PROTECTED) != 0;
    }

    private boolean isCapi()
    {
        return currentClassName != null && currentClassName.contains(".capi.") && !currentClassName.startsWith("tests.") && !currentClassName.contains(".test.");
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
    {
        // reference the annotation itself
        addClassName(getNormalizedClassName(desc), !visible);
        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
    {
        if (currentClassName != null)
        {
            String ft = getNormalizedClassName(Type.getType(desc).getClassName());
            if (isAcceptable(ft))
            {
                addClassName(ft, false);
            }
            if (isCapi() && (isPublic(access) || isProtected(access)))
            {
                Definition definition = new Definition();
                definition.setSignature(name);
                definition.setArtifact(isPublic(access) ? Artifact.PUBLIC_FIELD : Artifact.PROTECTED_FIELD);
                definition.setSource(currentClassName);
                javaClass.getApiDefinition().add(definition);
            }
        }
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        try
        {
            boolean isDeprecated = isDeprecated(access);
            if (currentClassName != null)
            {
                if (desc != null)
                {
                    for (String rt : JavaHelper.splitSignature(desc))
                    {
                        if (isAcceptable(rt))
                        {
                            addClassName(rt, isDeprecated);
                        }
                    }
                }
                if (signature != null)
                {
                    for (String rt : JavaHelper.splitSignature(signature))
                    {
                        if (isAcceptable(rt))
                        {
                            addClassName(rt, isDeprecated);
                        }
                    }
                }
                if (exceptions != null)
                {
                    for (String ex : exceptions)
                    {
                        String exClassName = getNormalizedClassName(ex);
                        if (isAcceptable(exClassName))
                        {
                            addClassName(exClassName, isDeprecated);
                        }
                    }
                }
                if (isCapi() && (isPublic(access) || isProtected(access)))
                {
                    Definition definition = new Definition();
                    definition.setSignature(name + "::" +desc);
                    definition.setArtifact(isPublic(access) ? Artifact.PUBLIC_METHOD : Artifact.PROTECTED_METHOD);
                    definition.setSource(currentClassName);
                    javaClass.getApiDefinition().add(definition);
                }
            }
            return new JavaMethodVisitor(isDeprecated);
        }
        catch(Exception e)
        {
            throw new RuntimeException("method name: '" + name + "' signature:'" + signature + "'", e);
        }
    }

    /* package */ static String getSignature(String className, String name, String desc)
    {
        String parameterAndReturn[] = desc.split("\\)");
        String parameter = null;
        String returnType = null;
        if (parameterAndReturn.length == 1)
        {
            parameter = ";";
            returnType = "";
        }
        else
        {
            parameter = parameterAndReturn[0];
            returnType = parameterAndReturn[1];
        }
        List<String> parameterList = Arrays.asList(parameter.substring(1).split(";")).stream().map(t -> getNormalizedClassName(t))
                        .collect(Collectors.toList());
        if (name.equals("<init>"))
        {
            return className + "(" + String.join(",", parameterList) + ")";
        }
        return getNormalizedClassName(returnType) + " " + name + "(" + String.join(",", parameterList) + ")";
    }

    /**
     * Returns true if the given class name should be contained in the
     * dependency matrix. Primitives are ignored.
     */
    private static boolean isAcceptable(String className)
    {
        return className.contains(".");
    }

    private void addClassName(String cn, boolean isDeprecated)
    {
        if (cn.equals(javaClass.getClassName()))
        {
            // don't register self references
            return;
        }
        if (isDeprecated)
        {
            javaClass.getDeprecatedRefs().add(cn);
        }
        else
        {
            javaClass.getUsageRefs().add(cn);
        }
    }

    private void addInterfaceName(String cn)
    {
        javaClass.getImplementsRef().add(cn);
        javaClass.getUsageRefs().add(cn);
    }

    /**
     * Inspects the method implementation for transitive dependencies, e.g. call
     * chains like "a().b().c()"
     */
    class JavaMethodVisitor extends MethodVisitor
    {
        final boolean isDeprecated;

        public JavaMethodVisitor(boolean isDeprecated)
        {
            super(API_VERSION);
            this.isDeprecated = isDeprecated;
        }

        // Checks if the method is an ExtensionPoint.
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            // reference the annotation itself
            String cn = getNormalizedClassName(desc);
            addClassName(cn, isDeprecated);

            if ("Lcom/intershop/platform/extension/capi/ExtensionPoint;".equals(desc))
            {
                return new ExtensionPointAnnotationVisitor(isDeprecated);
            }
            else
            {
                return null;
            }
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc)
        {
            String cn = getNormalizedClassName(owner);
            if (isAcceptable(cn))
            {
                addClassName(cn, isDeprecated);
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String signature)
        {
            for (String rt : JavaHelper.splitSignature(signature))
            {

                if (isAcceptable(rt))
                {
                    addClassName(rt, isDeprecated);
                }
            }
            String cn = getNormalizedClassName(owner);
            if (isAcceptable(cn))
            {
                addClassName(cn, isDeprecated);
            }
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims)
        {
            String cn = getNormalizedClassName(Type.getType(desc).getClassName());
            if (isAcceptable(cn))
            {
                addClassName(cn, isDeprecated);
            }
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
        {
            // ignore "finally"
            if (type != null)
            {
                String cn = getNormalizedClassName(type);
                if (isAcceptable(cn))
                {
                    addClassName(cn, isDeprecated);
                }
            }
        }

        @Override
        public void visitTypeInsn(int opcode, String type)
        {
            String cn = getNormalizedClassName(type);
            if (isAcceptable(cn))
            {
                addClassName(cn, isDeprecated);
            }
        }
    }

    /**
     * Inspects the method annotations for ExtensionPoints.
     */
    class ExtensionPointAnnotationVisitor extends AnnotationVisitor
    {
        private final boolean isDeprecated;

        public ExtensionPointAnnotationVisitor(boolean isDeprecated)
        {
            super(API_VERSION);
            this.isDeprecated = isDeprecated;
        }

        private String id;
        private String type;

        @Override
        public void visit(String name, Object value)
        {
            if ("id".equals(name))
            {
                id = String.valueOf(value);
            }
            else if ("type".equals(name))
            {
                Type t = (Type)value;
                type = getNormalizedClassName(t.getClassName());
            }
        }

        @Override
        public void visitEnd()
        {
            javaClass.getExtensionPointIDs().add(type + "-" + id);
            addClassName(type, isDeprecated);
        }
    }

    private static String getNormalizedClassName(String className)
    {
        return JavaHelper.getNormalizedClassName(className);
    }
}
