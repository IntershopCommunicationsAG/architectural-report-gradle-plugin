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

import java.util.concurrent.TimeoutException

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import org.gradle.process.JavaForkOptions
import org.gradle.process.internal.DefaultJavaForkOptions
import org.gradle.process.internal.JavaExecHandleBuilder

import com.intershop.tool.architecture.report.cmd.ArchitectureReport
import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants

import groovy.ui.SystemOutputInterceptor
import groovy.util.logging.Slf4j

/**
 * Task for api validation
 */
@Slf4j
class ValidateArchitectureTask extends DefaultTask {

    final static String TASK_DESCRIPTION = 'validate architecture'
    private static final String MAIN_CLASS_NAME = "com.intershop.tool.architecture.report.cmd.ArchitectureReport";

    @InputFile
    File ivyFile

    @InputDirectory
    File cartridgesDir

    @Input @Optional
    File baselineFile

    @Input @Optional
    File knownIssuesFile

    @Input
    List<String> keySelector

    @OutputDirectory
    File reportsDir

    boolean useExternalProcess = true;

    /**
     * Additional arguments
     */
    List<String> addVmArgs = []

    /**
     * Java fork options for the Java task.
     */
    JavaForkOptions javaOptions

    @Inject
    FileResolver getFileResolver() {
        throw new UnsupportedOperationException()
    }

    /**
     * Set Java fork options.
     *
     * @return JavaForkOptions
     */
    JavaForkOptions getJavaOptions() {
        if (javaOptions == null) {
            javaOptions = new DefaultJavaForkOptions(getFileResolver())
        }
        return javaOptions
    }

    @TaskAction
    void validateAPI() {
        if (useExternalProcess)
        {
            JavaExecHandleBuilder javaExec = new JavaExecHandleBuilder(getFileResolver())
            FileCollection classPath = getProject().getConfigurations().getAt(ArchitectureReportExtension.AR_EXTENSION_NAME)
            getJavaOptions().copyTo(javaExec)
            javaExec.setJvmArgs(getAddVmArgs())
            javaExec.setClasspath(classPath).setMain(MAIN_CLASS_NAME).setArgs(getArguments()).build().start().waitForFinish().assertNormalExitValue()
        } else {
            try
            {
                if (ArchitectureReport.validateArchitecture(getArguments()))
                {
                    throw new GradleException("Build contains architectural issues.");
                }
            } catch(Exception e)
            {
                throw new GradleException("Validation failed with execption:.", e);
            }
        }
    }

    private static void addArgument(List<String> arguments, String optionName, String value) {
        if (value) {
            arguments << '-' + optionName
            arguments << "${value}" // + ' ' + value
        }
    }

    private List<String> getArguments() {
        List<String> arguments = []
        addArgument(arguments, ArchitectureReportConstants.ARG_IVYFILE, getIvyFile().getAbsolutePath());
        addArgument(arguments, ArchitectureReportConstants.ARG_CARTRIDGE_DIRECTORY, getCartridgesDir().getAbsolutePath());
        if (getBaselineFile() != null)
        {
            addArgument(arguments, ArchitectureReportConstants.ARG_BASELINE, getBaselineFile().toURI().toString());
        }
        addArgument(arguments, ArchitectureReportConstants.ARG_OUTPUT_DIRECTORY, getReportsDir().getAbsolutePath());
        if(getKnownIssuesFile() != null)
        {
            addArgument(arguments, ArchitectureReportConstants.ARG_EXISTING_ISSUES_FILE, getKnownIssuesFile().toURI().toString());
        }
        addArgument(arguments, ArchitectureReportConstants.ARG_KEYS, String.join(",", getKeySelector()))
        addArgument(arguments, ArchitectureReportConstants.ARG_GROUP, project.group)
        addArgument(arguments, ArchitectureReportConstants.ARG_ARTIFACT, project.name)
        addArgument(arguments, ArchitectureReportConstants.ARG_VERSION, project.version)
        return arguments
    }
}
