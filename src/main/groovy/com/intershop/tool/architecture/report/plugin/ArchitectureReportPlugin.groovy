/*
 * Copyright 2015 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.intershop.tool.architecture.report.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet

/**
 * Plugin implementation
 */
class ArchitectureReportPlugin implements Plugin<Project> {

    /**
     * Applies the extension and calls the
     * task initialization for this plugin
     *
     * @param project
     */
    void apply (Project project) {
        project.logger.info('Create extension {} for {}', ArchitectureReportExtension.AR_EXTENSION_NAME, project.name)

        Task validateTask = project.tasks.findByName('validateArchitecture')
        if(! validateTask) {
            validateTask = project.getTasks().create('validateArchitecture', ValidateArchitectureTask)
            validateTask.group = ArchitectureReportExtension.AR_TASK_GROUP
            validateTask.description = ValidateArchitectureTask.TASK_DESCRIPTION
        }
        configureValidateArchitectureTask(project, validateTask)
    }

    /**
     * Configures tasks
     *
     * @param project
     * @param task
     */
    private void configureValidateArchitectureTask(Project project, ValidateArchitectureTask task) {
        ReportingExtension reportingExtension = project.extensions.getByType(ReportingExtension)
        ArchitectureReportExtension extension = project.extensions.findByType(ArchitectureReportExtension) ?:  project.extensions.create(ArchitectureReportExtension.AR_EXTENSION_NAME, ArchitectureReportExtension, project)
        task.conventionMapping.reportsDir = { extension.reportsDir == null ? reportingExtension.file(ArchitectureReportExtension.REPORTS_NAME) : extension.reportsDir }
        task.conventionMapping.ivyFile = { extension.ivyFile }
        task.conventionMapping.cartridgesDir = { extension.cartridgesDir }
        task.conventionMapping.baselineFile = { extension.baselineFile }
        task.conventionMapping.knownIssuesFile = { extension.knownIssuesFile }
        task.conventionMapping.keySelector = { extension.keySelector }
    }
}
