package com.intershop.tool.architecture.report.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.intershop.tool.architecture.report.api.model.actor.LibraryUpdateProcessor;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.issue.IssueCollector;
import com.intershop.tool.architecture.report.common.project.GlobalProcessor;
import com.intershop.tool.architecture.report.common.project.IvyVisitor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessor;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.isml.IsmlTemplateCollector;
import com.intershop.tool.architecture.report.java.JavaProjectCollector;
import com.intershop.tool.architecture.report.pipeline.PipelineProjectCollector;

public class ServerCollector implements IssueCollector
{
    private static final IvyVisitor IVY_VISITOR = new IvyVisitor();
/*    private final ReliableMessageActorRef<GetProjectsRequest> ivyActor= new ReliableMessageActorRef<>(getContext(), IvyActor.class, getSelf());
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
    private final ReliableMessageActorRef<PrintRequest> printActor = new ReliableMessageActorRef<>(getContext(),IssuePrinter.class, getSelf());
    private final LinkedList<ReliableMessageActorRef<?>> workingActors = new LinkedList<>(Arrays.asList(ivyActor, ismlFinder, jarFinder, pipelineFinder, jarActor, pipelineLoader, persistenceActor, boFilterActor, boValidatorActor, capiValidatorActor, pipeletFilterActor, unusedPipeletActor, groupPipeletActor, definitionCollectorActor, ismlActor, pipelineTester, printActor));
    private List<String> keySelector = Collections.emptyList();
    private ActorRef terminateActorRef = null;
*/

    private final CommandLineArguments info;

    public ServerCollector(CommandLineArguments info)
    {
        this.info = info;
    }
    

    private List<GlobalProcessor> getGlobalProcessor()
    {
        List<GlobalProcessor> result = new ArrayList<>();
        result.add(new LibraryUpdateProcessor(info));
        return result;
    }

    private List<ProjectProcessor> getProjectProcessor()
    {
        List<ProjectProcessor> result = new ArrayList<>();
        result.add(new IsmlTemplateCollector(info));
        result.add(new PipelineProjectCollector(info));
        result.add(new JavaProjectCollector(info));
        return result;
    }

    public List<Issue> validate()
    {
        List<Issue> result = new ArrayList<>();
        List<GlobalProcessor> globalProcessors = getGlobalProcessor();
        List<ProjectProcessor> projectProcessors = getProjectProcessor();

        ProjectProcessorResult projectResults = new ProjectProcessorResult();
        // process globals
        globalProcessors.forEach(c -> c.process(projectResults));
        Collection<ProjectRef> projects = IVY_VISITOR.apply(new File(info.getArgument(ArchitectureReportConstants.ARG_IVYFILE)));
        // process projects
        projects.forEach(p -> {
            projectProcessors.forEach(c -> c.process(p, projectResults));
        });
        // collect project issues
        projects.forEach(p -> {
            projectProcessors.forEach(c -> result.addAll(c.validate(p, projectResults)));
        });
        // collect globals
        globalProcessors.forEach(c -> result.addAll(c.validate(projectResults)));
        return result;
    }
/*

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
        ivyActor.tell(new GetProjectsRequest());
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
*/
}
