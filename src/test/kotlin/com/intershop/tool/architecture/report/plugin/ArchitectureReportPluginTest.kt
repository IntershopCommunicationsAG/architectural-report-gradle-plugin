package com.intershop.tool.architecture.report.plugin

import com.intershop.tool.architecture.report.tasks.ValidateArchitectureTask
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test

class ArchitectureReportPluginTest
{
    @Test
    fun canAddTaskToProject() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register(ValidateArchitectureTask.AR_TASK_NAME, ValidateArchitectureTask::class.java).get()
        assertThat(task, instanceOf(ValidateArchitectureTask::class.java))
    }
}
