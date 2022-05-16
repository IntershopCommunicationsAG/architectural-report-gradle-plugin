/*
 * JarFileVisitor.java
 *
 * Copyright (c) 2022 Intershop Communications AG
 */
package com.intershop.tool.architecture.report.java.model.jar;

import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.common.resources.XMLInputSourceVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClass;
import com.intershop.tool.architecture.report.java.model.jclass.JavaClassVisitor;
import com.intershop.tool.architecture.report.java.model.jclass.JavaHelper;
import com.intershop.tool.architecture.report.java.model.pipelet.PipeletDescriptor;
import com.intershop.tool.architecture.report.java.model.pipelet.PipeletHandler;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A file visitor that analysis Jar files.
 */
public class JarFileVisitor implements Function<ClassReader, JavaClass>
{
    private static final Logger logger = LoggerFactory.getLogger(JarFileVisitor.class);
    private final XMLInputSourceVisitor xmlVisitor = new XMLInputSourceVisitor();
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
        return visitFile(jarFile).getPipeletDescriptor();
    }

    public Jar visitFile(File file)
    {
        Jar result = new Jar(projectRef);
        try (JarFile jarFile = new JarFile(file))
        {
            logger.info("Reading files in JAR: {}", file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements())
            {
                JarEntry jarEntry = entries.nextElement();

                // ignore directories and non-class files
                if (jarEntry.isDirectory())
                {
                    continue;
                }
                // ignore Java module descriptor
                if (jarEntry.getName().endsWith("module-info.class"))
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
                if (jarEntry.getName().endsWith(".xml") && jarEntry.getName().contains("pipelet"))
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
                        result.getPipeletDescriptor().add(descriptor);
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
        try
        {
            JavaClass result = new JavaClass(getNormalizedClassName(cr.getClassName()),
                            getNormalizedClassName(cr.getSuperName()));
            cr.accept(new JavaClassVisitor(result), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return result;
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't read class: " + getNormalizedClassName(cr.getClassName()), e);
        }
    }

    private static String getNormalizedClassName(String className)
    {
        return JavaHelper.getNormalizedClassName(className);
    }
}
