/*
 * Copyright 2022 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.intershop.tool.architecture.report.plugin

import com.intershop.tool.architecture.report.tasks.CreateClasspathFileListTask
import com.intershop.tool.architecture.report.tasks.CreateDependenciesListTask
import com.intershop.tool.architecture.report.tasks.ValidateArchitectureTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.util.Properties

/**
 * Plugin implementation.
 */
class ArchitectureReportPlugin : Plugin<Project> {

    /**
     * Applies the extension and calls the task initialization for this plugin.
     *
     * @param project Current project
     */
    override fun apply(project: Project) {
        // the configuration must exist before the validation task is configured with it
        val reportToolConfiguration = createConfiguration(project)

        val createDependenciesListTask =
                project.tasks.register(CreateDependenciesListTask.TASK_NAME, CreateDependenciesListTask::class.java) {
                    it.group = CreateDependenciesListTask.TASK_GROUP
                    it.description = CreateDependenciesListTask.TASK_DESCRIPTION

                    // the coordinates are captured at configuration time, the task must not access the
                    // project during execution
                    it.projectGroup.set(project.provider { project.group.toString() })
                    it.projectName.set(project.provider { project.name })
                    it.projectVersion.set(project.provider { project.version.toString() })

                    // Depend on assemble task
                    it.dependsOn(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
                }
        configureCreateDependenciesListTask(project, createDependenciesListTask)

        val createClasspathFileListTask =
                project.tasks.register(CreateClasspathFileListTask.TASK_NAME, CreateClasspathFileListTask::class.java) {
                    it.group = CreateClasspathFileListTask.TASK_GROUP
                    it.description = CreateClasspathFileListTask.TASK_DESCRIPTION

                    wireRuntimeClasspath(project, it.classpathFiles)

                    // Depend on assemble task
                    it.dependsOn(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
                }

        val validateTask =
                project.tasks.register(ValidateArchitectureTask.AR_TASK_NAME, ValidateArchitectureTask::class.java) {
                    it.group = ValidateArchitectureTask.AR_TASK_GROUP
                    it.description = ValidateArchitectureTask.AR_TASK_DESCRIPTION

                    it.projectGroup.set(project.provider { project.group.toString() })
                    it.projectName.set(project.provider { project.name })
                    it.projectVersion.set(project.provider { project.version.toString() })

                    wireRuntimeClasspath(project, it.classpathFiles)
                    it.reportToolClasspath.from(reportToolConfiguration)

                    // Depend on dependency list creation (and therefore also assemble) task
                    it.dependsOn(createDependenciesListTask)
                    it.dependenciesFile.set(project.provider {
                        createDependenciesListTask.get().outputFile.get()
                    })
                    // Depend on classpath file list creation (and therefore also assemble) task
                    it.dependsOn(createClasspathFileListTask)
                    it.classpathFilesListFile.set(project.provider {
                        createClasspathFileListTask.get().classpathFilesListFile.get()
                    })
                }

        configureValidateArchitectureTask(project, validateTask)
    }

    /*
     * Wires the Java runtime classpath and the jar of the project at configuration time. The tasks must
     * not resolve the configuration themselves, because that would happen during input snapshotting in
     * the execution phase.
     *
     * The wiring is done for the java plugin only, which may be applied after this plugin.
     */
    private fun wireRuntimeClasspath(project: Project, classpathFiles: ConfigurableFileCollection) {
        project.plugins.withType(JavaPlugin::class.java) {
            classpathFiles.from(
                    project.configurations.named(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME),
                    project.tasks.named(JavaPlugin.JAR_TASK_NAME).map { it.outputs.files.singleFile }
            )
        }
    }

    /**
     * Create configuration for validation tasks.
     *
     * @param project Current project
     * @return the configuration that provides the architecture report tool
     */
    private fun createConfiguration(project: Project): Configuration {
        val configuration = project.configurations.maybeCreate(ArchitectureReportExtension.AR_EXTENSION_NAME)
        if (configuration.allDependencies.isEmpty()) {
            // Get version of AR plugin itself to add the versioned plugin as dependency to the applied project
            val props = javaClass.classLoader.getResourceAsStream("version.properties").use {
                Properties().apply { load(it) }
            }
            val pluginVersion = props.getProperty("version")

            configuration
                    .setTransitive(true)
                    .setDescription("Validate architecture with architecture report")
                    .defaultDependencies { dependencies ->
                        // Only this plugin is declared. The configuration is transitive and the published
                        // module of this plugin already lists its own runtime dependencies (slf4j, asm,
                        // commons-io, jaxb, icm-gradle-plugin, logback, kotlin-stdlib, ...), so they are
                        // resolved from there. Declaring them again here would duplicate the versions of
                        // build.gradle.kts and let them drift apart.
                        dependencies.add(project.dependencies.create(
                                "com.intershop.gradle.architectural.report:architectural-report-gradle-plugin:${pluginVersion}"))
                    }
        }
        return configuration
    }

    /**
     * Configures validation tasks.
     *
     * @param project Current project
     * @param task Validate architecture task
     */
    private fun configureValidateArchitectureTask(project: Project, task: TaskProvider<ValidateArchitectureTask>) {
        val extension = project.extensions.findByType(ArchitectureReportExtension::class.java)
                        ?: project.extensions.create(ArchitectureReportExtension.AR_EXTENSION_NAME,
                                ArchitectureReportExtension::class.java, project)

        task.configure {
            with(it) {
                dependenciesFile.set(extension.dependenciesFile)
                baselineFile.set(extension.baselineFile)
                knownIssuesFile.set(extension.knownIssuesFile)
                keySelector.set(extension.keySelector)
                reportsDirectory.set(extension.reportsDirectory)

                useExternalProcess.set(extension.useExternalProcess)
                additionalJvmArguments.set(extension.additionalJvmArguments)
            }
        }
    }

    /**
     * Configures create dependencies list tasks.
     *
     * @param project Current project
     * @param task Create dependencies list task
     */
    private fun configureCreateDependenciesListTask(project: Project, task: TaskProvider<CreateDependenciesListTask>) {
        val extension = project.extensions.findByType(ArchitectureReportExtension::class.java)
                        ?: project.extensions.create(ArchitectureReportExtension.AR_EXTENSION_NAME,
                                ArchitectureReportExtension::class.java, project)

        task.configure {
            it.outputFile.set(extension.dependenciesFile)
        }
    }
}
