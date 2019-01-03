package com.intershop.tool.architecture.report.cmd;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.akka.actors.tooling.AkkaMessage;
import com.intershop.tool.architecture.report.common.messages.PrintResponse;
import com.intershop.tool.architecture.report.server.ServerActor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import akka.actor.Terminated;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ArchitectureReport
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchitectureReport.class);
    private static boolean buildFailed = false;
    private static boolean buildFinished = false;

    /**
     * Execute Architecture report on commandline
     * @param args command line arguments
     * <ul>
     *            <li>server ivy file: d:\Source\ish\gradle_trunk\server\share\ivy.xml</li>
     *            <li>cartridge directory: d:\Source\ish\gradle_trunk\server\share\system\cartridges</li>
     *            <li>api baseline resource e.g. api_definition_baseline_7.7.xml</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if (ArchitectureReport.validateArchitecture(args))
        {
            System.exit(1);
        }
    }

    /**
     * @param outputDirectory output directory
     * @param ivyFile ivy dependency declaration for deployment
     * @param cartridgesDirectory directory contains cartridges and libs
     * @param baselineFile file contains api baseline
     * @param knownIssuesFile file contains known issues (will be ignored)
     * @param keySelector list of keys, which will be validated
     * @return true in case validation is failing
     */
    public static boolean validateArchitecture(File outputDirectory, File ivyFile, File cartridgesDirectory, File baselineFile, File knownIssuesFile, List<String> keySelector)
    {
        CommandLineArguments info = new CommandLineArguments();
        info.setArgument(ArchitectureReportConstants.ARG_IVYFILE, ivyFile.getAbsolutePath());
        info.setArgument(ArchitectureReportConstants.ARG_CARTRIDGE_DIRECTORY, cartridgesDirectory.getAbsolutePath());
        if (baselineFile != null)
        {
            info.setArgument(ArchitectureReportConstants.ARG_BASELINE, baselineFile.toURI().toString());
        }
        info.setArgument(ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY, outputDirectory.getAbsolutePath());
        if(knownIssuesFile != null)
        {
            info.setArgument(ArchitectureReportConstants.ARG_EXISTING_ISSUES_FILE, knownIssuesFile.toURI().toString());
        }
        info.setArgument(ArchitectureReportConstants.ARG_KEYS, String.join(",", keySelector));
        return validateArchitecture(info);
    }

    /**
     * @param args program arguments
     * @return true in case validation is failing
     */
    public static boolean validateArchitecture(String... args)
    {
        CommandLineArguments info = new CommandLineArguments(args);
        return validateArchitecture(info);
    }

    public static boolean validateArchitecture(CommandLineArguments info)
    {
        final ActorSystem system = ActorSystem.create("ArchitectureReport");
        // Create the 'greeter' actor
        final ActorRef serverActor = system.actorOf(Props.create(ServerActor.class), "server");

        // Create the "actor-in-a-box"
        final Inbox inbox = Inbox.create(system);
        inbox.send(serverActor, info);
        inbox.send(serverActor, AkkaMessage.TERMINATE.FLUSH_REQUEST);
        try
        {
            while(processResponse(inbox))
            {
                // wait until messages are processed
            }
        }
        catch(TimeoutException e)
        {
            LOGGER.error("Error during architecture report", e);
            buildFailed = true;
        }
        finally
        {
            Future<Terminated> isTerminated = system.terminate();
            while (!isTerminated.isCompleted() && !buildFinished)
            {
                try {
                    processResponse(inbox);
                }
                catch (TimeoutException e) {
                    // nothing to do
                }
            }
        }
        return buildFailed;
    }

    private static boolean processResponse(final Inbox inbox) throws TimeoutException
    {
        Object message = inbox.receive(Duration.create(2, TimeUnit.MINUTES));
        if (message == null)
        {
            LOGGER.info("Architecture report doesn't receive messages for 2 minutes.");
            return true;
        }
        if (message instanceof String)
        {
            LOGGER.error("message: {}", message);
            return true;
        }
        else if (AkkaMessage.TERMINATE.FLUSH_RESPONSE.equals(message))
        {
            LOGGER.info("REPORT FINISH");
            buildFinished = true;
            return false;
        }
        else if (message instanceof PrintResponse)
        {
            if (!((PrintResponse)message).getIssues().isEmpty())
            {
                buildFailed = true;
            }
        }
        return true;
    }
}
