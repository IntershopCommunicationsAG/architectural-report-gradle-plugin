package com.intershop.tool.architecture.report.tasks

import com.intershop.tool.architecture.report.plugin.ArchitectureReportExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task which creates list of classpath files (jars determined from classpath).
 */
open class CreateClasspathFileListTask : DefaultTask() {
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
     */
    @get:Classpath
    val classpathFiles: FileCollection by lazy {
        project.files().from(
                project.configurations.findByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME),
                project.tasks.named("jar").get().outputs.files.singleFile
        )
    }

    /**
     * Store list of classpath files in a temporary file in order to pass it as argument
     * in case the string exceeds the maximum length of an CLI argument of the OS.
     */
    @OutputFile
    val classpathFilesListFile: RegularFileProperty = project.objects.fileProperty().convention(project.provider {
        project.layout.buildDirectory.dir(ArchitectureReportExtension.AR_DIRECTORY_NAME).get()
                .file("classpath_files.txt")
    })

    /**
     * Retrieve classpath entries via [classpathFiles] and write them to [classpathFilesListFile]
     */
    @TaskAction
    fun createClasspathFileList() {
        // Write list of classpath files to temporary text file
        classpathFilesListFile.get().asFile.writeText(classpathFiles.joinToString(separator = System.lineSeparator()))
    }

}