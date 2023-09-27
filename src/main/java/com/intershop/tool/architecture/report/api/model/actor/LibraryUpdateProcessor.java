package com.intershop.tool.architecture.report.api.model.actor;

import com.intershop.tool.architecture.report.api.model.definition.APIDefinition;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.api.model.definition.DefinitionSorting;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportOutputFolder;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.DependencyListVisitor;
import com.intershop.tool.architecture.report.common.project.GlobalProcessor;
import com.intershop.tool.architecture.report.common.project.LibDefinitionMapper;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.common.resources.URILoader;
import com.intershop.tool.architecture.report.common.resources.XMLLoaderException;
import com.intershop.tool.architecture.report.common.resources.XmlLoader;
import com.intershop.tool.architecture.versions.UpdateStrategy;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates if project contains new libraries or library updates which violate a semantic versioning {@link UpdateStrategy}.
 */
public class LibraryUpdateProcessor implements GlobalProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(LibraryUpdateProcessor.class);
    private final CommandLineArguments info;
    private final ProjectRef serverProject;

    private static class Configuration
    {
        private ArchitectureReportOutputFolder folderLocations;
        private UpdateStrategy strategy = UpdateStrategy.MINOR;
        private final List<Definition> baseline = new ArrayList<>();
    }

    public LibraryUpdateProcessor(CommandLineArguments info)
    {
        this.info = info;
        this.serverProject = new ProjectRef(
                        info.getArgument(ArchitectureReportConstants.ARG_GROUP),
                        info.getArgument(ArchitectureReportConstants.ARG_ARTIFACT),
                        info.getArgument(ArchitectureReportConstants.ARG_VERSION));
    }

    public void process(ProjectProcessorResult projectResult)
    {
        projectResult.definitions.addAll(getDefinitions());
    }

    /**
     * @return true in case validation identifies errors
     */
    public List<Issue> validate(ProjectProcessorResult projectResult)
    {
        Configuration config = getConfiguration();
        Collection<Definition> definitions = projectResult.definitions;
        DefinitionComparer issueCollector = new DefinitionComparer(serverProject, definitions, config.baseline, config.strategy);
        try
        {
            exportDefinition(config.folderLocations.getApiDefinitionFile(), definitions);
            exportDefinition(config.folderLocations.getApiViolationFile(), issueCollector.getAPIViolations());
        }
        catch(IOException | JAXBException e)
        {
            logger.warn("Can't export files", e);
        }
        return issueCollector.getIssues();
    }

    private List<Definition> getDefinitions()
    {
        if (info.getArgument(ArchitectureReportConstants.ARG_DEPENDENCIES_FILE) == null) {
            return Collections.emptyList();
        }

        logger.info("Processing dependencies from file: {}", info.getArgument(ArchitectureReportConstants.ARG_DEPENDENCIES_FILE));
        File dependenciesListFile = new File(info.getArgument(ArchitectureReportConstants.ARG_DEPENDENCIES_FILE));
        Collection<ProjectRef> projects = new DependencyListVisitor().apply(dependenciesListFile);

        LibDefinitionMapper definitionMapper = new LibDefinitionMapper(serverProject);
        return projects.stream().map(definitionMapper).collect(Collectors.toList());
    }

    private Configuration getConfiguration()
    {
        Configuration result = new Configuration();
        result.folderLocations = new ArchitectureReportOutputFolder(info.getArgument(ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY));
        String baselineLocation = info.getArgument(ArchitectureReportConstants.ARG_BASELINE);
        if (baselineLocation != null)
        {
            XmlLoader xmlLoader = new XmlLoader();
            try
            {
                InputStream is = URILoader.getInputStream(baselineLocation);
                APIDefinition baselineDefinition = xmlLoader.importXML(is, APIDefinition.class);
                result.baseline.addAll(baselineDefinition.getDefinition());
            }
            catch(InterruptedException e)
            {
                logger.warn("Loading API definition '{}' failed due to thread interruption.", baselineLocation, e);
                Thread.currentThread().interrupt();
            }
            catch(XMLLoaderException | IOException e)
            {
                logger.warn("Loading API definition '{}' failed.", baselineLocation, e);
            }
        }
        String strategyArg = info.getArgument(ArchitectureReportConstants.ARG_STRATEGY);
        if (strategyArg != null)
        {
            result.strategy = UpdateStrategy.valueOf(strategyArg);
        }
        return result;
    }

    private static void exportDefinition(File file, Collection<Definition> definitions) throws IOException, JAXBException
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
