/*
 * JarFileVisitor.java
 *
 * Copyright (c) 2010 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.java.model.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;

import org.objectweb.asm.ClassReader;
import org.xml.sax.SAXException;

import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.common.resources.XMLInputSourceVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClassVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaHelper;
import com.intershop.tool.architecture.report.java.model.pipelet.PipeletDescriptor;
import com.intershop.tool.architecture.report.java.model.pipelet.PipeletHandler;

/**
 * A file visitor that analysis Jar files.
 */
public class JarFileVisitor implements Function<ClassReader, JavaClass>
{
    private XMLInputSourceVisitor xmlVisitor = new XMLInputSourceVisitor();
    private final ProjectRef projectRef;

    public JarFileVisitor(ProjectRef projectRef)
    {
        this.projectRef = projectRef;
    }

    public Collection<JavaClass> getClasses(String jarResource)
    {
        return getClasses(new File(getClass().getClassLoader().getResource(jarResource).getFile()));
    }

    public Collection<JavaClass> getClasses(File jarFile) throws JarParsingException
    {
        return visitFile(jarFile).getClasses();
    }

    public Collection<PipeletDescriptor> getPipeletDescriptors(String jarResource)
    {
        return getPipeletDescriptors(new File(getClass().getClassLoader().getResource(jarResource).getFile()));
    }

    public Collection<PipeletDescriptor> getPipeletDescriptors(File jarFile)
    {
        return visitFile(jarFile).getPipeletDesciptor();
    }

    public Jar visitFile(File file)
    {
        Jar result = new Jar(projectRef);
        try (JarFile jarFile = new JarFile(file))
        {
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements())
            {
                JarEntry jarEntry = entries.nextElement();

                // ignore directories and non class files
                if (jarEntry.isDirectory())
                {
                    continue;
                }
                if (jarEntry.getName().endsWith(".class"))
                {
                    String className = jarEntry.getName().replace('/', '.'); // including ".class"
                    className = className.substring(0, className.length() - ".class".length());
                    try (InputStream in = jarFile.getInputStream(jarEntry))
                    {
                        ClassReader cr = new ClassReader(in);
                        result.add(apply(cr));
                    }
                    catch (IllegalArgumentException e) {
                        throw new IOException("Can't read jar entry: " + jarEntry.getName(), e);
                    }
                }
                if (jarEntry.getName().endsWith(".xml"))
                {
                    PipeletDescriptor descriptor = new PipeletDescriptor();
                    try
                    {
                        xmlVisitor.visitXMLInputSource(jarFile.getInputStream(jarEntry), new PipeletHandler(projectRef.getName(), descriptor));
                    } catch (SAXException e) {
                        throw new IOException("Parsing xml of '" + jarEntry.getName() + "'", e);
                    }
                    if (descriptor.getReferenceName() != null)
                    {
                        result.getPipeletDesciptor().add(descriptor);
                    }
                }
            }
        }
        catch(ParserConfigurationException | IOException e)
        {
            throw new JarParsingException("Loading jar failed '"+file.getAbsolutePath()+"'", e);
        }
        return result;
    }

    @Override
    public JavaClass apply(ClassReader cr)
    {
        JavaClass result = new JavaClass(getNormalizedClassName(cr.getClassName()),
                        getNormalizedClassName(cr.getSuperName()));
        try
        {
            cr.accept(new JavaClassVisitor(result), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't read class: " + getNormalizedClassName(cr.getClassName()), e);
        }
        return result;
    }

    private static String getNormalizedClassName(String className)
    {
        return JavaHelper.getNormalizedClassName(className);
    }
}
