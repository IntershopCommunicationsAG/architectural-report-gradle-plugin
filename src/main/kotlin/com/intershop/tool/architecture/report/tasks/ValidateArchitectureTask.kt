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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.work.DisableCachingByDefault
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject // gradle9 requires javax.inject.Inject

/**
 * Task for architecture validation.
 */
@DisableCachingByDefault(because = "Analyses the whole runtime classpath and writes a report; " +
        "the result depends on the concrete project layout and is not worth caching")
abstract class ValidateArchitectureTask @Inject constructor(
        private val execOps : ExecOperations,
        objectFactory: ObjectFactory) : DefaultTask() {
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
    val keySelector: ListProperty<String> = objectFactory.listProperty(String::class.java)

    /**
     * Specifies dependencies file whereas each line represents a project dependency.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    val dependenciesFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * Specifies classpath list file whereas each line represents a classpath entry (jar file).
     */
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    val classpathFilesListFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * API baseline file.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    val baselineFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * Known issues file to ignore listed issues.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    val knownIssuesFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * Whether to use external execution handler for validation process.
     */
    @Optional
    @Input
    val useExternalProcess: Property<Boolean> = objectFactory.property(Boolean::class.java)

    /**
     * Additional JVM arguments.
     */
    @Optional
    @Input
    val additionalJvmArguments: ListProperty<String> = objectFactory.listProperty(String::class.java)

    /**
     * Output directory to write reports to.
     */
    @Optional
    @OutputDirectory
    val reportsDirectory: DirectoryProperty = objectFactory.directoryProperty()

    /**
     * File collection of Java runtime classpath files.
     *
     * The plugin wires this at configuration time. The task must not resolve the configuration itself,
     * because that would happen during input snapshotting in the execution phase, where accessing
     * {@code Task.project} is deprecated in Gradle 9 and fails in Gradle 10.
     *
     * @property classpathFiles
     */
    @get:Classpath
    val classpathFiles: ConfigurableFileCollection = objectFactory.fileCollection()

    /**
     * Classpath of the architecture report tool, used when it is started in a child process.
     *
     * The plugin wires this at configuration time, see {@link #classpathFiles}.
     *
     * This property must not be an input: it would then be resolved while the task inputs are snapshotted,
     * which resolves the architecture report tool even when the validation runs in the Gradle process
     * ({@code useExternalProcess = false}) and never needs it.
     *
     * @property reportToolClasspath
     */
    @get:Internal
    val reportToolClasspath: ConfigurableFileCollection = objectFactory.fileCollection()

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
     * Validate architecture.
     */
    @TaskAction
    open fun validateArchitecture() {

        val args = getArguments()
        try {
            if (useExternalProcess.get()) {
                val javaExec: ExecResult = execOps.javaexec { exec ->
                    exec.mainClass.set(MAIN_CLASS_NAME)
                    exec.classpath(reportToolClasspath)

                    exec.jvmArgs(additionalJvmArguments.get())
                    exec.args(args.toList())

                    exec.standardOutput = System.out
                    exec.errorOutput = System.err

                    log.info("Architecture Report validation started in child process with arguments: {}", args)
                }
                javaExec.assertNormalExitValue()
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
        addArgument(arguments, ArchitectureReportConstants.ARG_ARTIFACT, projectName.get())
        addArgument(arguments, ArchitectureReportConstants.ARG_GROUP, projectGroup.get())
        addArgument(arguments, ArchitectureReportConstants.ARG_VERSION, projectVersion.get())
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
