package com.intershop.tool.architecture.report.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.akka.actors.tooling.ReliableMessageActorRef;
import com.intershop.tool.architecture.report.api.messages.APIDefinitionRequest;
import com.intershop.tool.architecture.report.api.messages.APIDefinitionResponse;
import com.intershop.tool.architecture.report.api.model.actor.DefinitionCollectorActor;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.actors.IssuePrinterActor;
import com.intershop.tool.architecture.report.common.messages.FileRequest;
import com.intershop.tool.architecture.report.common.messages.NewIssuesResponse;
import com.intershop.tool.architecture.report.common.messages.PrintFixedIssueRequest;
import com.intershop.tool.architecture.report.common.messages.PrintIssueRequest;
import com.intershop.tool.architecture.report.common.messages.PrintNewIssuesRequest;
import com.intershop.tool.architecture.report.common.messages.PrintRequest;
import com.intershop.tool.architecture.report.common.messages.PrintResponse;
import com.intershop.tool.architecture.report.isml.actors.IsmlTemplateActor;
import com.intershop.tool.architecture.report.isml.messages.GetIsmlTemplatesRequest;
import com.intershop.tool.architecture.report.isml.messages.GetIsmlTemplatesResponse;
import com.intershop.tool.architecture.report.isml.messages.IsmlValidationResponse;
import com.intershop.tool.architecture.report.jar.JarActor;
import com.intershop.tool.architecture.report.jar.messages.GetJarResponse;
import com.intershop.tool.architecture.report.java.model.JavaClassRequest;
import com.intershop.tool.architecture.report.java.validation.bo.BusinessObjectValidatorActor;
import com.intershop.tool.architecture.report.java.validation.bo.ValidateBusinessObjectResponse;
import com.intershop.tool.architecture.report.java.validation.bo.identification.BusinessObjectFilterActor;
import com.intershop.tool.architecture.report.java.validation.bo.identification.IsBusinessObjectResponse;
import com.intershop.tool.architecture.report.java.validation.capi.CapiValidatorActor;
import com.intershop.tool.architecture.report.java.validation.capi.ValidateCapiResponse;
import com.intershop.tool.architecture.report.java.validation.pipelet.IsPipeletResponse;
import com.intershop.tool.architecture.report.java.validation.pipelet.PipeletFilterActor;
import com.intershop.tool.architecture.report.java.validation.po.IsPersistenceResponse;
import com.intershop.tool.architecture.report.java.validation.po.PersistenceFilterActor;
import com.intershop.tool.architecture.report.java.validation.unused.ValidateUnusedResponse;
import com.intershop.tool.architecture.report.pipelet.actors.PipeletGroupActor;
import com.intershop.tool.architecture.report.pipelet.actors.UnusedPipeletValidatorActor;
import com.intershop.tool.architecture.report.pipeline.PipelineActor;
import com.intershop.tool.architecture.report.pipeline.messages.GetPipelinesRequest;
import com.intershop.tool.architecture.report.pipeline.messages.GetPipelinesResponse;
import com.intershop.tool.architecture.report.pipeline.messages.PipelineResponse;
import com.intershop.tool.architecture.report.pipeline.messages.PipelineTestRequest;
import com.intershop.tool.architecture.report.pipeline.messages.PipelineTestResponse;
import com.intershop.tool.architecture.report.pipeline.validation.PipelineTestActor;
import com.intershop.tool.architecture.report.project.actors.IvyActor;
import com.intershop.tool.architecture.report.project.actors.ProjectActor;
import com.intershop.tool.architecture.report.project.messages.GetJarsRequest;
import com.intershop.tool.architecture.report.project.messages.GetJarsResponse;
import com.intershop.tool.architecture.report.project.messages.GetProjectsRequest;
import com.intershop.tool.architecture.report.project.messages.GetProjectsResponse;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public class ServerActor extends AbstractActor
{
    private ActorRef terminateActorRef = null;
    private final ReliableMessageActorRef<GetProjectsRequest> ivyActor= new ReliableMessageActorRef<>(getContext(), IvyActor.class, getSelf());
    private final ReliableMessageActorRef<GetIsmlTemplatesRequest> ismlFinder = new ReliableMessageActorRef<>(getContext(), ProjectActor.class, getSelf());
    private final ReliableMessageActorRef<GetJarsRequest> jarFinder = new ReliableMessageActorRef<>(getContext(), ProjectActor.class, getSelf());
    private final ReliableMessageActorRef<GetPipelinesRequest> pipelineFinder = new ReliableMessageActorRef<>(getContext(), ProjectActor.class, getSelf());
    private final ReliableMessageActorRef<FileRequest> jarActor = new ReliableMessageActorRef<>(getContext(),JarActor.class, getSelf());
    private final ReliableMessageActorRef<FileRequest> pipelineLoader = new ReliableMessageActorRef<>(getContext(),PipelineActor.class, getSelf());
    private final ReliableMessageActorRef<JavaClassRequest> persistenceActor = new ReliableMessageActorRef<>(getContext(),PersistenceFilterActor.class, getSelf());
    private final ReliableMessageActorRef<JavaClassRequest> pipeletFilterActor = new ReliableMessageActorRef<>(getContext(),PipeletFilterActor.class, getSelf());
    private final ReliableMessageActorRef<JavaClassRequest> boFilterActor = new ReliableMessageActorRef<>(getContext(),BusinessObjectFilterActor.class, getSelf());
    private final ReliableMessageActorRef<JavaClassRequest> boValidatorActor = new ReliableMessageActorRef<>(getContext(),BusinessObjectValidatorActor.class, getSelf());
    private final ReliableMessageActorRef<JavaClassRequest> capiValidatorActor = new ReliableMessageActorRef<>(getContext(),CapiValidatorActor.class, getSelf());
    private final ReliableMessageActorRef<JavaClassRequest> unusedPipeletActor = new ReliableMessageActorRef<>(getContext(),UnusedPipeletValidatorActor.class, getSelf());
    private final ReliableMessageActorRef<?> groupPipeletActor = new ReliableMessageActorRef<>(getContext(),PipeletGroupActor.class, getSelf());
    private final ReliableMessageActorRef<APIDefinitionRequest> definitionCollectorActor = new ReliableMessageActorRef<>(getContext(),DefinitionCollectorActor.class, getSelf());
    private final ReliableMessageActorRef<FileRequest> ismlActor = new ReliableMessageActorRef<>(getContext(),IsmlTemplateActor.class, getSelf());
    private final ReliableMessageActorRef<PipelineTestRequest> pipelineTester = new ReliableMessageActorRef<>(getContext(),PipelineTestActor.class, getSelf());
    private final ReliableMessageActorRef<PrintRequest> printActor = new ReliableMessageActorRef<>(getContext(),IssuePrinterActor.class, getSelf());

    private final LinkedList<ReliableMessageActorRef<?>> workingActors = new LinkedList<>(Arrays.asList(ivyActor, ismlFinder, jarFinder, pipelineFinder, jarActor, pipelineLoader, persistenceActor, boFilterActor, boValidatorActor, capiValidatorActor, pipeletFilterActor, unusedPipeletActor, groupPipeletActor, definitionCollectorActor, ismlActor, pipelineTester, printActor));
    private List<String> keySelector = Collections.emptyList();

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                        .match(CommandLineArguments.class, this::receive)
                        .match(GetJarsResponse.class, this::receive)
                        .match(GetPipelinesResponse.class, this::receive)
                        .match(GetJarResponse.class, this::receive)
                        .match(IsPersistenceResponse.class, this::receive)
                        .match(IsPipeletResponse.class, this::receive)
                        .match(IsBusinessObjectResponse.class, this::receive)
                        .match(ValidateBusinessObjectResponse.class, this::receive)
                        .match(PrintResponse.class, this::receive)
                        .match(ValidateCapiResponse.class, this::receive)
                        .match(ValidateUnusedResponse.class, this::receive)
                        .match(PipelineResponse.class, this::receive)
                        .match(APIDefinitionResponse.class, this::receive)
                        .match(GetIsmlTemplatesResponse.class, this::receive)
                        .match(IsmlValidationResponse.class, this::receive)
                        .match(PipelineTestResponse.class, this::receive)
                        .match(NewIssuesResponse.class, this::receive)
                        .match(GetProjectsResponse.class, this::receive)
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_REQUEST, message -> handleFlushRequest())
                        .matchEquals(AkkaMessage.TERMINATE.FLUSH_RESPONSE, message -> handleFlushResponse())
                        .build();
    }

    private void receive(NewIssuesResponse message)
    {
        printActor.tell(new PrintIssueRequest(message.getIssues()));
    }

    private void receive(IsmlValidationResponse message)
    {
        ismlActor.receive(message.getRequest());
        printActor.tell(new PrintIssueRequest(message.getIssues()));
    }

    private void receive(GetIsmlTemplatesResponse message)
    {
        ProjectRef projectRef = message.getRequest().getProjectRef();
        ismlFinder.receive(message.getRequest());
        List<FileRequest> requests = message.getFileNames().stream().map(fn -> new FileRequest(fn, projectRef)).collect(Collectors.toList());
        ismlActor.tell(requests);
    }

    private void receive(APIDefinitionResponse message)
    {
        printActor.tell(new PrintIssueRequest(message.getIssues()));
    }

    private void receive(IsPipeletResponse message)
    {
        pipeletFilterActor.receive(message.getRequest());
        if (message.isPipelet())
        {
            unusedPipeletActor.tell(message.getRequest());
        }
    }

    private void receive(ValidateUnusedResponse message)
    {
        if (message.getRequest() != null)
        {
            unusedPipeletActor.receive(message.getRequest());
            groupPipeletActor.tellOtherMessage(message);
        }
        else
        {
            printActor.tell(new PrintIssueRequest(message.getIssues()));
        }
    }

    private void receive(PrintResponse message)
    {
        printActor.receive(message.getRequest());
        terminateActorRef.tell(message, getSelf());
    }

    private void receive(ValidateBusinessObjectResponse message)
    {
        boValidatorActor.receive(message.getRequest());
        printActor.tell(new PrintIssueRequest(message.getIssues()));
    }

    private void receive(ValidateCapiResponse message)
    {
        capiValidatorActor.receive(message.getRequest());
        printActor.tell(new PrintIssueRequest(message.getIssues()));
    }

    private void receive(IsBusinessObjectResponse message)
    {
        boFilterActor.receive(message.getRequest());
        if (message.isBusinessObject())
        {
            boValidatorActor.tell(message.getRequest());
            capiValidatorActor.tell(message.getRequest());
        }
    }

    private void receive(IsPersistenceResponse message)
    {
        persistenceActor.receive(message.getRequest());
        boValidatorActor.tellOtherMessage(message);
    }

    private void receive(GetJarResponse message)
    {
        ProjectRef projectRef = message.getJar().getProjectRef();
        jarActor.receive(message.getRequest());
        Collection<JavaClassRequest> javaClassRequests = message.getJar().getClasses().stream().map(jc -> new JavaClassRequest(jc, projectRef)).collect(Collectors.toList());
        persistenceActor.tell(javaClassRequests);
        boFilterActor.tell(javaClassRequests);
        pipeletFilterActor.tell(javaClassRequests);
        unusedPipeletActor.tellOtherMessage(message);
        definitionCollectorActor.tell(message.getJar().getClasses().stream().map(jc -> new APIDefinitionRequest(jc.getApiDefinition())).collect(Collectors.toList()));
    }

    private void receive(GetJarsResponse message)
    {
        jarFinder.receive(message.getRequest());
        ProjectRef projectRef = message.getRequest().getProjectRef();
        jarActor.tell(message.getFileNames().stream().map(jarFilePath -> new FileRequest(jarFilePath, projectRef)).collect(Collectors.toList()));
    }

    private void receive(GetPipelinesResponse message)
    {
        pipelineFinder.receive(message.getRequest());
        ProjectRef projectRef = message.getRequest().getProjectRef();
        pipelineLoader.tell(message.getFileNames().stream().map(jarFilePath -> new FileRequest(jarFilePath, projectRef)).collect(Collectors.toList()));
    }

    private void receive(PipelineResponse message)
    {
        pipelineLoader.receive(message.getRequest());
        unusedPipeletActor.tellOtherMessage(message);
        pipelineTester.tell(new PipelineTestRequest(message.getPipeline(), message.getRequest().getProjectRef()));
    }
    private void receive(PipelineTestResponse message)
    {
        pipelineTester.receive(message.getRequest());
    }

    private void receive(CommandLineArguments info)
    {
        String keys = info.getArgument(ArchitectureReportConstants.ARG_KEYS);
        if (keys != null)
        {
            keySelector = Arrays.asList(keys.split(","));
        }
        jarFinder.tellOtherMessage(info);
        pipelineFinder.tellOtherMessage(info);
        ismlFinder.tellOtherMessage(info);
        definitionCollectorActor.tellOtherMessage(info);
        printActor.tellOtherMessage(info);
        ivyActor.tell(new GetProjectsRequest(info.getArgument(ArchitectureReportConstants.ARG_IVYFILE)));
    }

    private boolean isIsmlCheckEnabled()
    {
        return keySelector.isEmpty() || keySelector.contains(ArchitectureReportConstants.KEY_XSS);
    }

    private boolean isPipelineCheckEnabled()
    {
        return keySelector.isEmpty() || keySelector.contains(ArchitectureReportConstants.KEY_INVALID_PIPELINEREF);
    }

    private void receive(GetProjectsResponse message)
    {
        ivyActor.receive(message.getRequest());
        message.getProjects().forEach(project -> jarFinder.tell(new GetJarsRequest(project)));
        if (isPipelineCheckEnabled())
        {
            message.getProjects().forEach(project -> pipelineFinder.tell(new GetPipelinesRequest(project)));
        }
        else
        {
            LoggerFactory.getLogger(getClass()).info("PIPELINES IGNORED");
        }
        if (isIsmlCheckEnabled())
        {
            message.getProjects().forEach(project -> ismlFinder.tell(new GetIsmlTemplatesRequest(project)));
        }
        else
        {
            LoggerFactory.getLogger(getClass()).info("ISML IGNORED");
        }
        message.getProjects().forEach(
                        project -> definitionCollectorActor.tell(new APIDefinitionRequest(message.getDefinitions())));
    }

    private boolean flushPrint = false;
    private void handleFlushResponse()
    {
        ReliableMessageActorRef<?> actor = workingActors.getFirst();
        actor.flushResponse(getSender());
        if(actor.isFinished())
        {
            workingActors.removeFirst();
        }
        if(workingActors.isEmpty())
        {
            if (flushPrint)
            {
                LoggerFactory.getLogger(getClass()).info("SERVER FINISHED");
                terminateActorRef.tell(AkkaMessage.TERMINATE.FLUSH_RESPONSE, getSelf());
            }
            else
            {
                // last action in case all workingActors are finished
                printActor.tell(new PrintFixedIssueRequest());
                printActor.tell(new PrintNewIssuesRequest());
                printActor.flush();
                workingActors.add(printActor);
                flushPrint = true;
            }
        }
        else
        {
            workingActors.getFirst().flush();
        }
    }

    private void handleFlushRequest()
    {
        terminateActorRef = getSender();
        workingActors.getFirst().flush();
    }
}