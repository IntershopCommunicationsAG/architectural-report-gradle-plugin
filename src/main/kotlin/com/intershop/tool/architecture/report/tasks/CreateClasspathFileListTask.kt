package com.intershop.tool.architecture.report.tasks

import com.intershop.tool.architecture.report.plugin.ArchitectureReportExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Task which creates list of classpath files (jars determined from classpath).
 */
@DisableCachingByDefault(because = "Writes the absolute paths of the runtime classpath entries, " +
        "which are machine specific and therefore not cacheable")
abstract class CreateClasspathFileListTask @Inject constructor(
        objectFactory: ObjectFactory,
        projectLayout: ProjectLayout) : DefaultTask() {
    companion object {
        /**
         * Task name
         */
        const val TASK_NAME = "createClasspathFileList"

        /**
         * Task group name
         */
        const val TASK_GROUP = "Verification"

        /**
         * Task description
         */
        const val TASK_DESCRIPTION =
                "Creates a list of classpath files (jars determined from classpath) to be consumed by Architecture Report Tool"
    }

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
     * Store list of classpath files in a temporary file in order to pass it as argument
     * in case the string exceeds the maximum length of an CLI argument of the OS.
     */
    @OutputFile
    val classpathFilesListFile: RegularFileProperty = objectFactory.fileProperty().convention(
            projectLayout.buildDirectory.dir(ArchitectureReportExtension.AR_DIRECTORY_NAME)
                    .map { it.file("classpath_files.txt") })

    /**
     * Retrieve classpath entries via [classpathFiles] and write them to [classpathFilesListFile]
     */
    @TaskAction
    fun createClasspathFileList() {
        // Write list of classpath files to temporary text file
        classpathFilesListFile.get().asFile.writeText(classpathFiles.joinToString(separator = System.lineSeparator()))
    }

}