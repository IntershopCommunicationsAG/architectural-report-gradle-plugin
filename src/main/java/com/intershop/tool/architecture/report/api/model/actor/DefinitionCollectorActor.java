package com.intershop.tool.architecture.report.api.model.actor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.api.messages.APIDefinitionRequest;
import com.intershop.tool.architecture.report.api.messages.APIDefinitionResponse;
import com.intershop.tool.architecture.report.api.model.definition.APIDefinition;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.api.model.definition.DefinitionSorting;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportOutputFolder;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.model.URILoader;
import com.intershop.tool.architecture.report.common.model.XMLLoaderException;
import com.intershop.tool.architecture.report.common.model.XmlLoader;
import com.intershop.tool.architecture.versions.UpdateStrategy;

import akka.actor.AbstractActor;

/**
 * BusinessObjectValidatorActor receives validation events for business objects.
 * The received messages contains business objects only.
 */
public class DefinitionCollectorActor extends AbstractActor
{
    private static final Set<Definition> definitions = new HashSet<>();
    private static final Set<Definition> baseline = new HashSet<>();

    private ArchitectureReportOutputFolder folderLocations;
    private boolean callFinishOnReceiveLocation = false;
    private UpdateStrategy strategy = UpdateStrategy.MINOR;
    private boolean taskStarted = false;

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(CommandLineArguments.class, this::receive)
                        .match(APIDefinitionRequest.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> {
                            finish();
                            getSender().tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
                        })
                        .build();
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
        String strategyArg = request.getArgument(ArchitectureReportConstants.ARG_STRATEGY);
        if (strategyArg != null)
        {
            strategy = UpdateStrategy.valueOf(strategyArg);
        }
        if (callFinishOnReceiveLocation)
        {
            finish();
        }
    }

    private void receive(APIDefinitionRequest request)
    {
        request.getDefinitions().stream().forEach(d -> registerDefinition(d));
    }

    private static void registerDefinition(Definition definition)
    {
        definitions.add(definition);
    }

    private void finish()
    {
        // avoid race conditions between both events, do not collect issues twice
        if (taskStarted)
        {
            return;
        }
        if (folderLocations != null)
        {
            taskStarted = true;
            DefinitionCollectorIssueCollector issueCollector = new DefinitionCollectorIssueCollector(definitions, baseline, strategy);
            APIDefinitionResponse message = new APIDefinitionResponse(definitions, issueCollector.getAPIViolations(), issueCollector.getIssues());
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
            definitions.clear();
        }
        else
        {
            callFinishOnReceiveLocation  = true;
        }
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
