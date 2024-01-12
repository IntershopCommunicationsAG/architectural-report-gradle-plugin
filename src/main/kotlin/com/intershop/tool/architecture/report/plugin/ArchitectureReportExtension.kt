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

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * This extension provides the container for all architecture report related configurations.
 */
open class ArchitectureReportExtension(val project: Project) {
    companion object {
        /**
         * Extension name
         */
        const val AR_EXTENSION_NAME: String = "architectureReport"

        /**
         * Reports directory name
         */
        const val AR_DIRECTORY_NAME: String = "architectureReport"

        /**
         * Dependencies file to be shared between tasks by default
         */
        const val AR_DEPENDENCIES_FILENAME: String = "dependencies.txt"
    }

    val keySelector: ListProperty<String> = project.objects.listProperty(String::class.java)

    val dependenciesFile: RegularFileProperty = project.objects.fileProperty()

    val baselineFile: RegularFileProperty = project.objects.fileProperty()

    val knownIssuesFile: RegularFileProperty = project.objects.fileProperty()

    val useExternalProcess: Property<Boolean> = project.objects.property(Boolean::class.java)

    val additionalJvmArguments: ListProperty<String> = project.objects.listProperty(String::class.java)

    val reportsDirectory: DirectoryProperty = project.objects.directoryProperty()

    init {
        useExternalProcess.convention(true)
        dependenciesFile.convention(
                project.layout.buildDirectory.dir(AR_DIRECTORY_NAME).get().file(AR_DEPENDENCIES_FILENAME))
        reportsDirectory.convention(project.layout.buildDirectory.dir(AR_DIRECTORY_NAME))
    }
}
