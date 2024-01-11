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
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.util.*

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
        val createDependenciesListTask =
                project.tasks.register(CreateDependenciesListTask.TASK_NAME, CreateDependenciesListTask::class.java) {
                    it.group = CreateDependenciesListTask.TASK_GROUP
                    it.description = CreateDependenciesListTask.TASK_DESCRIPTION

                    // Depend on assemble task
                    it.dependsOn(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
                }
        configureCreateDependenciesListTask(project, createDependenciesListTask)

        val createClasspathFileListTask =
                project.tasks.register(CreateClasspathFileListTask.TASK_NAME, CreateClasspathFileListTask::class.java) {
                    it.group = CreateClasspathFileListTask.TASK_GROUP
                    it.description = CreateClasspathFileListTask.TASK_DESCRIPTION

                    // Depend on assemble task
                    it.dependsOn(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
                }

        val validateTask =
                project.tasks.register(ValidateArchitectureTask.AR_TASK_NAME, ValidateArchitectureTask::class.java) {
                    it.group = ValidateArchitectureTask.AR_TASK_GROUP
                    it.description = ValidateArchitectureTask.AR_TASK_DESCRIPTION

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
        createConfiguration(project)
    }

    /**
     * Create configuration for validation tasks.
     *
     * @param project Current project
     */
    private fun createConfiguration(project: Project) {
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
                    val dependencyHandler = project.dependencies

                    dependencies.add(dependencyHandler.create("com.intershop.gradle.architectural.report:architectural-report-gradle-plugin:${pluginVersion}"))
                    dependencies.add(dependencyHandler.create("org.slf4j:slf4j-api:2.0.9"))
                    dependencies.add(dependencyHandler.create("org.ow2.asm:asm:9.5"))
                    dependencies.add(dependencyHandler.create("javax.inject:javax.inject:1"))
                    dependencies.add(dependencyHandler.create("commons-io:commons-io:2.13.0"))
                    dependencies.add(dependencyHandler.create("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1"))
                    dependencies.add(dependencyHandler.create("org.glassfish.jaxb:jaxb-runtime:4.0.3"))
                    dependencies.add(dependencyHandler.create("com.intershop.gradle.icm:icm-gradle-plugin:5.8.0"))

                    dependencies.add(dependencyHandler.create("ch.qos.logback:logback-classic:1.4.11"))
                }
        }
    }

    /**
     * Configures validation tasks.
     *
     * @param project Current project
     * @param task Validate architecture task
     */
    private fun configureValidateArchitectureTask(project: Project, task: TaskProvider<ValidateArchitectureTask>) {
        val extension = project.extensions.findByType(ArchitectureReportExtension::class.java)
            ?: project.extensions.create(ArchitectureReportExtension.AR_EXTENSION_NAME, ArchitectureReportExtension::class.java, project)

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
