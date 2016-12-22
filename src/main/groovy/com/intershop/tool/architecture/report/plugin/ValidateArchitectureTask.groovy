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

import com.intershop.tool.architecture.report.plugin.ArchitectureReportExtension
import com.intershop.tool.architecture.report.cmd.ArchitectureReport
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.api.GradleException

/**
 * Task for api validation
 */
@Slf4j
class ValidateArchitectureTask extends DefaultTask {

	final static String TASK_DESCRIPTION = 'validate architecture'

    @InputFile
    File ivyFile

    @InputDirectory
    File cartridgesDir

    @Input
    File baselineFile

    @Input
    File knownIssuesFile

    @Input
    List<String> keySelector

    @OutputDirectory
    File reportsDir

    @TaskAction
    void validateAPI() {
        if (ArchitectureReport.validateArchitecture(getReportsDir(), getIvyFile(), getCartridgesDir(), getBaselineFile(), getKnownIssuesFile(), getKeySelector()))
        {
            throw new GradleException("Build contains architectural issues.");
        }
    }
}
