package com.intershop.tool.architecture.report.api.model.actor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.report.api.model.definition.APIDefinition;
import com.intershop.tool.architecture.report.api.model.definition.Definition;
import com.intershop.tool.architecture.report.api.model.definition.DefinitionSorting;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.cmd.ArchitectureReportOutputFolder;
import com.intershop.tool.architecture.report.cmd.CommandLineArguments;
import com.intershop.tool.architecture.report.common.issue.Issue;
import com.intershop.tool.architecture.report.common.project.GlobalProcessor;
import com.intershop.tool.architecture.report.common.project.IvyVisitor;
import com.intershop.tool.architecture.report.common.project.LibDefinitionMapper;
import com.intershop.tool.architecture.report.common.project.ProjectProcessorResult;
import com.intershop.tool.architecture.report.common.project.ProjectRef;
import com.intershop.tool.architecture.report.common.resources.URILoader;
import com.intershop.tool.architecture.report.common.resources.XMLLoaderException;
import com.intershop.tool.architecture.report.common.resources.XmlLoader;
import com.intershop.tool.architecture.versions.UpdateStrategy;

/**
 * Validates Modification of Java Classes
 */
public class LibraryUpdateProcessor implements GlobalProcessor
{
    private static final IvyVisitor IVY_VISITOR = new IvyVisitor();
    private final CommandLineArguments info;
    private final ProjectRef serverProject;

    private static class Configuration
    {
        private ArchitectureReportOutputFolder folderLocations;
        private List<Definition> baseline = new ArrayList<>();
        private UpdateStrategy strategy = UpdateStrategy.MINOR;
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
            LoggerFactory.getLogger(getClass()).warn("Can't export files", e);
        }
        return issueCollector.getIssues();
    }

    private List<Definition> getDefinitions()
    {
        if (info.getArgument(ArchitectureReportConstants.ARG_IVYFILE) == null)
        {
            return Collections.emptyList();
        }
        Collection<ProjectRef> projects = IVY_VISITOR.apply(new File(info.getArgument(ArchitectureReportConstants.ARG_IVYFILE)));
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
            catch(XMLLoaderException | IOException e)
            {
                LoggerFactory.getLogger(getClass()).warn("loading api definition failed, location:" + baselineLocation, e);
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
