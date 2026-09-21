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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.jar.JarOutputStream

/**
 * Integration tests that run the tasks of the plugin in a real build.
 *
 * The tests are deliberately free of external dependencies, so that they neither need network access nor
 * a prepared repository. They cover the task inputs that are derived from the project - the runtime
 * classpath, the jar of the project and its group/name/version.
 */
class ArchitectureReportPluginIntegrationTest {

    companion object {
        private const val GROUP = "com.intershop.test"
        private const val VERSION = "1.0.0"
        private const val PROJECT_NAME = "testproject"

        private const val REPORT_DIR = "build/architectureReport"
    }

    @TempDir
    lateinit var testProjectDir: File

    /**
     * Writes a minimal Java project that applies the plugin.
     *
     * @param additionalConfiguration additional build script content
     */
    private fun writeProject(additionalConfiguration: String = "") {
        File(testProjectDir, "settings.gradle.kts")
                .writeText("rootProject.name = \"$PROJECT_NAME\"")

        File(testProjectDir, "build.gradle.kts").writeText("""
            plugins {
                java
                id("com.intershop.gradle.architectural.report")
            }

            group = "$GROUP"
            version = "$VERSION"

            $additionalConfiguration
        """.trimIndent())

        val sourceDir = File(testProjectDir, "src/main/java/com/example")
        sourceDir.mkdirs()
        File(sourceDir, "Simple.java").writeText("package com.example; public class Simple {}")
    }

    private fun run(vararg arguments: String): BuildResult =
            GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withPluginClasspath()
                    .withArguments(*arguments, "-s")
                    .build()

    @Test
    fun `createClasspathFileList writes the runtime classpath and the jar of the project`() {
        // an additional file dependency puts an entry on the runtime classpath without network access.
        // It must be a valid archive, because it ends up on the compile classpath of the test project.
        val libraryFile = File(testProjectDir, "libs/dummy-library.jar")
        libraryFile.parentFile.mkdirs()
        JarOutputStream(libraryFile.outputStream()).close()

        writeProject("""
            dependencies {
                implementation(files("libs/dummy-library.jar"))
            }
        """.trimIndent())

        val result = run("createClasspathFileList")

        assertEquals(TaskOutcome.SUCCESS, result.task(":createClasspathFileList")?.outcome)

        val classpathFilesListFile = File(testProjectDir, "$REPORT_DIR/classpath_files.txt")
        assertTrue(classpathFilesListFile.exists(), "classpath file list is written")

        val entries = classpathFilesListFile.readLines()
        assertTrue(entries.any { it.endsWith("dummy-library.jar") },
                "runtime classpath entry is listed, but was: $entries")
        assertTrue(entries.any { it.endsWith("$PROJECT_NAME-$VERSION.jar") },
                "jar of the project is listed, but was: $entries")
    }

    @Test
    fun `createDependenciesList writes the project itself as self dependency`() {
        // the task resolves the cartridge runtime configuration, which is normally created by the ICM
        // cartridge plugin - here it is created directly to keep the test free of that dependency
        writeProject("""
            configurations.create("cartridgeRuntime")
        """.trimIndent())

        val result = run("createDependenciesList")

        assertEquals(TaskOutcome.SUCCESS, result.task(":createDependenciesList")?.outcome)

        val dependenciesFile = File(testProjectDir, "$REPORT_DIR/dependencies.txt")
        assertTrue(dependenciesFile.exists(), "dependency list is written")

        assertEquals(listOf("self:$GROUP:$PROJECT_NAME:$VERSION"), dependenciesFile.readLines(),
                "the project itself is listed as 'self' dependency")
    }

    @Test
    fun `validateArchitecture passes the project coordinates to the report tool`() {
        writeProject("""
            configurations.create("cartridgeRuntime")

            architectureReport {
                // fail fast instead of downloading and running the report tool
                additionalJvmArguments.set(listOf("-DinvalidOption"))
            }
        """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .withArguments("validateArchitecture", "-s", "--info")
                .buildAndFail()

        // the arguments are logged before the report tool is started
        assertTrue(result.output.contains("-artifact, $PROJECT_NAME"),
                "artifact argument is derived from the project name, but output was:\n${result.output}")
        assertTrue(result.output.contains("-group, $GROUP"),
                "group argument is derived from the project group, but output was:\n${result.output}")
        assertTrue(result.output.contains("-version, $VERSION"),
                "version argument is derived from the project version, but output was:\n${result.output}")
    }
}
