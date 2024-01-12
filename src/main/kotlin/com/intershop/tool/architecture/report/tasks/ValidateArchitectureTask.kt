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
package com.intershop.tool.architecture.report.tasks

import com.intershop.tool.architecture.report.cmd.ArchitectureReport
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants
import com.intershop.tool.architecture.report.plugin.ArchitectureReportExtension.Companion.AR_EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Task for architecture validation.
 */
open class ValidateArchitectureTask : DefaultTask() {
    companion object {
        /**
         * Task name
         */
        const val AR_TASK_NAME = "validateArchitecture"

        /**
         * Task group name
         */
        const val AR_TASK_GROUP = "Verification"

        /**
         * Task description
         */
        const val AR_TASK_DESCRIPTION = "Validate architecture"

        /**
         * Main class
         */
        const val MAIN_CLASS_NAME = "com.intershop.tool.architecture.report.cmd.ArchitectureReport"

        /**
         * Logger
         */
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    /**
     * Defines keys for validation.
     */
    @Input
    val keySelector: ListProperty<String> = project.objects.listProperty(String::class.java)

    /**
     * Specifies dependencies file whereas each line represents a project dependency.
     */
    @Optional
    @InputFile
    val dependenciesFile: RegularFileProperty = project.objects.fileProperty()

    /**
     * Specifies classpath list file whereas each line represents a classpath entry (jar file).
     */
    @InputFile
    val classpathFilesListFile: RegularFileProperty = project.objects.fileProperty()

    /**
     * API baseline file.
     */
    @Optional
    @InputFile
    val baselineFile: RegularFileProperty = project.objects.fileProperty()

    /**
     * Known issues file to ignore listed issues.
     */
    @Optional
    @InputFile
    val knownIssuesFile: RegularFileProperty = project.objects.fileProperty()

    /**
     * Whether to use external execution handler for validation process.
     */
    @Optional
    @Input
    val useExternalProcess: Property<Boolean> = project.objects.property(Boolean::class.java)

    /**
     * Additional JVM arguments.
     */
    @Optional
    @Input
    val additionalJvmArguments: ListProperty<String> = project.objects.listProperty(String::class.java)

    /**
     * Output directory to write reports to.
     */
    @Optional
    @OutputDirectory
    val reportsDirectory: DirectoryProperty = project.objects.directoryProperty()

    /**
     * File collection of Java runtime classpath files.
     */
    @get:Classpath
    val classpathFiles: FileCollection by lazy {
        project.files().from(
                project.configurations.findByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME),
                project.tasks.named("jar").get().outputs.files.singleFile
        )
    }

    /**
     * Validate architecture.
     */
    @TaskAction
    open fun validateArchitecture() {

        val args = getArguments()
        try {
            if (useExternalProcess.get()) {
                try {
                    val javaExec: ExecResult = project.javaexec { exec ->
                        exec.mainClass.set(MAIN_CLASS_NAME)
                        exec.classpath(project.configurations.getByName(AR_EXTENSION_NAME))

                        exec.jvmArgs(additionalJvmArguments.get())
                        exec.args(args.toList())

                        exec.standardOutput = System.out
                        exec.errorOutput = System.err

                        log.info("Architecture Report validation started in child process with arguments: {}", args)
                    }
                    javaExec.assertNormalExitValue()
                } catch (e: ExecException) {
                    throw GradleException("Build contains architectural issues.")
                }
            } else {
                log.info("Architecture Report validation started in Gradle process with arguments: {}", args)
                if (ArchitectureReport.validateArchitecture(args)) {
                    throw GradleException("Build contains architectural issues.")
                }
            }
        } catch (e: Exception) {
            throw GradleException("Validation failed with exception: " + e.message, e)
        }
    }

    /**
     * Shorthand to add argument.
     *
     * @param arguments List of arguments to add to
     * @param optionName Argument name
     * @param value Value
     */
    private fun addArgument(arguments: ArrayList<String>, optionName: String, value: String) {
        arguments.addAll(listOf("-$optionName", value))
    }

    /**
     * Build array of arguments to pass it to ART via command line.
     *
     * @return Array of arguments
     */
    private fun getArguments(): Array<String> {
        val arguments = arrayListOf<String>()
        addArgument(arguments, ArchitectureReportConstants.ARG_ARTIFACT, project.name)
        addArgument(arguments, ArchitectureReportConstants.ARG_GROUP, project.group as String)
        addArgument(arguments, ArchitectureReportConstants.ARG_VERSION, project.version as String)
        addArgument(arguments, ArchitectureReportConstants.ARG_KEYS, keySelector.get().joinToString(separator = ","))
        addArgument(arguments, ArchitectureReportConstants.ARG_DEPENDENCIES_FILE,
                dependenciesFile.get().asFile.absolutePath)
        addArgument(arguments, ArchitectureReportConstants.ARG_CLASSPATH_FILES_LIST_FILE,
                classpathFilesListFile.get().asFile.absolutePath)
        if (baselineFile.isPresent) {
            addArgument(arguments, ArchitectureReportConstants.ARG_BASELINE, baselineFile.get().asFile.absolutePath)
        }
        if (knownIssuesFile.isPresent) {
            addArgument(arguments, ArchitectureReportConstants.ARG_EXISTING_ISSUES_FILE,
                    knownIssuesFile.get().asFile.absolutePath)
        }
        addArgument(arguments, ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY,
                reportsDirectory.get().asFile.absolutePath)

        return arguments.toTypedArray()
    }
}
