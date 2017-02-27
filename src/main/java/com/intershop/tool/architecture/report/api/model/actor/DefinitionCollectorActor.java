package com.intershop.tool.architecture.report.api.model.actor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.api.messages.APIDefinitionResponse;
import com.intershop.tool.architecture.report.api.model.definition.APIDefinition;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.api.model.definition.DefinitionSorting;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportOutputFolder;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.common.model.URILoader;
import com.intershop.tool.architecture.report.common.model.XMLLoaderException;
import com.intershop.tool.architecture.report.common.model.XmlLoader;
import com.intershop.tool.architecture.report.jar.messages.GetJarResponse;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

import akka.actor.UntypedActor;

/**
 * BusinessObjectValidatorActor receives validation events for business objects.
 * The received messages contains business objects only.
 */
public class DefinitionCollectorActor extends UntypedActor
{
    private static final List<Definition> definitions = new ArrayList<>();
    private static final Set<Definition> baseline = new HashSet<>();
    private ArchitectureReportOutputFolder folderLocations;
    private boolean callFinishOnReceiveLocation = false;

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof JavaClassRequest)
        {
            JavaClassRequest request = (JavaClassRequest)message;
            receive(request);
        }
        else if (message instanceof GetJarResponse)
        {
            GetJarResponse request = (GetJarResponse)message;
            receive(request);
        }
        else if (message instanceof CommandLineArguments)
        {
            CommandLineArguments request = (CommandLineArguments)message;
            receive(request);
        }
        else if (AkkaMessage.TERMINATE.FLUSH_REQUEST.equals(message))
        {
            finish();
            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
        }
        else
        {
            unhandled(message);
        }
    }

    private void receive(CommandLineArguments request)
    {
        folderLocations = new ArchitectureReportOutputFolder(request.getArgument(ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY));
        String baselineLocation = request.getArgument(ArchitectureReportConstants.ARG_BASELINE);
        if (baselineLocation != null)
        {
            XmlLoader xmlLoader = new XmlLoader();
            try
            {
                InputStream is = URILoader.getInputStream(baselineLocation);
                APIDefinition baselineDefinition = xmlLoader.importXML(is, APIDefinition.class);
                baseline.addAll(baselineDefinition.getDefinition());
            }
            catch(XMLLoaderException|IOException e)
            {
                LoggerFactory.getLogger(getClass()).warn("loading api definition failed, location:" + baselineLocation, e);
            }
        }
        if (callFinishOnReceiveLocation)
        {
            finish();
        }
    }

    private static void receive(GetJarResponse request)
    {
        request.getJar().getClasses().stream().forEach(jc -> registerDefinition(jc.getApiDefinition()));
    }

    private static void receive(JavaClassRequest request)
    {
        registerDefinition(request.getJavaClass().getApiDefinition());
    }

    private static void registerDefinition(APIDefinition apiDefinition)
    {
        apiDefinition.getDefinition().stream().forEach(d -> registerDefinition(d));
    }

    private static void registerDefinition(Definition definition)
    {
        definitions.add(definition);
        baseline.remove(definition);
    }

    private void finish()
    {
        List<Issue> issues = getIssues();
        APIDefinitionResponse message = new APIDefinitionResponse(definitions, baseline, issues);
        if (folderLocations != null)
        {
            try
            {
                exportDefinition(folderLocations.getApiDefinitionFile(), message.getCollectedDefinitions());
                exportDefinition(folderLocations.getApiViolationFile(), message.getRemovedDefinitions());
            }
            catch(IOException| JAXBException e)
            {
                LoggerFactory.getLogger(getClass()).warn("Can't export definition files", e);
            }
            getSender().tell(message, getSelf());
        }
        else
        {
            callFinishOnReceiveLocation  = true;
        }
    }

    private static List<Issue> getIssues()
    {
        Map<String, ProjectRef> touchedClasses = new HashMap<>();
        baseline.stream().forEach(d -> touchedClasses.put(d.getSource(), d.getProjectRef()));
        return touchedClasses.entrySet().stream()
                        .map(entry -> new Issue(entry.getValue(), ArchitectureReportConstants.KEY_API_VIOLATION, entry.getKey()))
                        .collect(Collectors.toList());
    }

    private static void exportDefinition(File file, Collection<Definition> definitions)
                    throws IOException, JAXBException
    {
        FileWriter writer = new FileWriter(file);
        APIDefinition definition = new APIDefinition();
        definition.getDefinition().addAll(definitions);
        definition.getDefinition().sort(DefinitionSorting.DEFINITION_COMPARATOR);
        XmlLoader loader = new XmlLoader();
        loader.exportXML(definition, writer);
        writer.flush();
        writer.close();
    }
}
