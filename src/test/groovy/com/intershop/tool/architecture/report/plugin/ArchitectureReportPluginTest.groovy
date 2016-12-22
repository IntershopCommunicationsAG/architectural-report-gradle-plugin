package com.intershop.tool.architecture.report.plugin;

import org.gradle.api.Project
import org.junit.Test;
import org.gradle.testfixtures.ProjectBuilder;
import static org.junit.Assert.assertTrue;

class ArchitectureReportPluginTest
{
    @Test
    public void canAddTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('validateArchitecture', type: ValidateArchitectureTask)
        assertTrue(task instanceof ValidateArchitectureTask)
    }
}
