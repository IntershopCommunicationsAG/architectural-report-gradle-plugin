package com.intershop.tool.architecture.report.jar;

import java.io.File;
import java.util.Collection;

import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.common.actors.AbstractFileActor;
import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.jar.messages.GetJarResponse;
import com.intershop.tool.architecture.report.jar.model.Jar;
import com.intershop.tool.architecture.report.jar.model.JarFileVisitor;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class JarActor extends AbstractFileActor
{
    @Override
    protected void receive(FileRequest request)
    {
        JarFileVisitor javaVisitor = new JarFileVisitor(request.getProjectRef());
        File jarFile = new File(request.getFileName());
        Jar visitFile = javaVisitor.visitFile(jarFile);
        enrichWithProjectRef(visitFile, request.getProjectRef());
        getSender().tell(new GetJarResponse(request, visitFile), getSelf());
    }

    private static void enrichWithProjectRef(Jar visitFile, ProjectRef projectRef)
    {
        visitFile.getClasses().stream().forEach(jc -> enrichWithProjectRef(jc.getApiDefinition(), projectRef));
    }

    private static void enrichWithProjectRef(Collection<Definition> definitions, ProjectRef projectRef)
    {
        definitions.stream().forEach(d -> d.setProjectRef(projectRef));
    }
}