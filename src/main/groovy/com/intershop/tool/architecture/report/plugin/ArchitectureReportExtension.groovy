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

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * This extension provides the container for all architecture report related configurations.
 */
class ArchitectureReportExtension {

    /**
     * Extension name
     */
    final static String AR_EXTENSION_NAME = 'architectureReport'

    /**
     * Reports directory name
     */
    final static String REPORTS_NAME = 'architectureReport'

    /**
     * Task group name
     */
    final static String AR_TASK_GROUP = 'Verification'

    private Project project

    /**
     * Initialize the extension.
     *
     * @param project
     */
    public ArchitectureReportExtension(Project project) {

        this.project = project
    }

    File ivyFile

    File cartridgesDir

    File baselineFile

    File knownIssuesFile

    List<String> keySelector

    File reportsDir

    Boolean useExternalProcess = true

    List<String> addVmArgs = []
}
