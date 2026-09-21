package com.intershop.tool.architecture.report.tasks

import com.intershop.gradle.icm.ICMBasePlugin.Companion.CONFIGURATION_CARTRIDGE_RUNTIME
import com.intershop.gradle.icm.utils.CartridgeUtil
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Task which creates list of project dependencies (libraries, cartridges).
 */
@DisableCachingByDefault(because = "Resolves the runtime and cartridge configurations of the project " +
        "and has no declared inputs, so its result cannot be safely cached")
abstract class CreateDependenciesListTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {
    companion object {
        /**
         * Task name
         */
        const val TASK_NAME = "createDependenciesList"

        /**
         * Task group name
         */
        const val TASK_GROUP = "Verification"

        /**
         * Task description
         */
        const val TASK_DESCRIPTION = "Create list of dependencies to be consumed by Architecture Report Tool"
    }

    /*
     * The configurations and the dependency handler are captured at configuration time, so that the task
     * does not access the project during execution. They cannot be serialized by the configuration cache,
     * which is why this task declares itself as incompatible with it.
     */
    private val configurations: ConfigurationContainer = project.configurations
    private val dependencyHandler: DependencyHandler = project.dependencies

    init {
        notCompatibleWithConfigurationCache(
                "This task resolves the runtime and cartridge configurations of the project during execution.")
    }

    /**
     * Group of the project this task belongs to.
     */
    @get:Input
    val projectGroup: Property<String> = objectFactory.property(String::class.java)

    /**
     * Name of the project this task belongs to.
     */
    @get:Input
    val projectName: Property<String> = objectFactory.property(String::class.java)

    /**
     * Version of the project this task belongs to.
     */
    @get:Input
    val projectVersion: Property<String> = objectFactory.property(String::class.java)

    /**
     * File to write dependency list to.
     */
    @OutputFile
    val outputFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * Retrieve all library and cartridge dependencies and write list to output file.
     */
    @TaskAction
    fun createDependenciesFile() {
        val dependencies = ArrayList<String>()
        // Add cartridge instance itself to cartridge dependencies
        dependencies.add("self:${projectGroup.get()}:${projectName.get()}:${projectVersion.get()}")

        // Add library and cartridge dependencies
        dependencies.addAll(getLibraryDependencies())
        dependencies.addAll(getCartridgeDependencies())

        // Write dependencies to output file line by line
        outputFile.get().asFile.writeText(dependencies.joinToString(separator = System.lineSeparator()))
    }

    /**
     * Create set of library dependencies of project.
     */
    private fun getLibraryDependencies(): HashSet<String> {
        val dependencies = HashSet<String>()
        val resolvedConfig =
                configurations.getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME).resolvedConfiguration
        // Ensure build fails if there are resolve errors
        if (resolvedConfig.hasError()) {
            resolvedConfig.rethrowFailure()
        }
        resolvedConfig.lenientConfiguration.allModuleDependencies.forEach { dependency ->
            dependency.moduleArtifacts.forEach { artifact ->
                when (val identifier = artifact.id.componentIdentifier) {
                    is ModuleComponentIdentifier -> {
                        if (artifact.extension.equals("jar") &&
                            !CartridgeUtil.isCartridge(dependencyHandler, logger, identifier)) {
                            dependencies.add("library:${identifier.group}:${identifier.module}:${identifier.version}")
                        }
                    }
                }
            }
        }

        return dependencies
    }

    /**
     * Create set of cartridge dependencies of project.
     */
    private fun getCartridgeDependencies(): HashSet<String> {
        val dependencies = HashSet<String>()
        val resolvedConfig = configurations.getByName(CONFIGURATION_CARTRIDGE_RUNTIME).resolvedConfiguration
        // Ensure build fails if there are resolve errors
        if (resolvedConfig.hasError()) {
            resolvedConfig.rethrowFailure()
        }
        resolvedConfig.lenientConfiguration.allModuleDependencies.forEach { dependency ->
            dependency.moduleArtifacts.forEach { artifact ->
                val componentIdentifier = artifact.id.componentIdentifier
                val identifier = artifact.moduleVersion.id
                when (componentIdentifier) {
                    is ProjectComponentIdentifier ->
                        dependencies.add("cartridge:${identifier.group}:${identifier.name}:${identifier.version}")

                    is ModuleComponentIdentifier ->
                        if (CartridgeUtil.isCartridge(dependencyHandler, logger, componentIdentifier)) {
                            dependencies.add("cartridge:${identifier.group}:${identifier.name}:${identifier.version}")
                        }
                }
            }
        }

        return dependencies
    }
}