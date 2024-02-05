import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Copyright 2022 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

plugins {
    // project plugins
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.22"

    // test coverage
    jacoco

    // ide plugin
    idea
    eclipse

    // publish plugin
    `maven-publish`

    // artifact signing - necessary on Maven Central
    signing

    // plugin for publishing to Gradle Portal
    id("com.gradle.plugin-publish") version "1.2.1"
}


// release configuration
group = "com.intershop.gradle.architectural.report"
description = "Gradle architectural report plugin"
// apply gradle property 'projectVersion' to project.version, default to 'LOCAL'
val projectVersion : String? by project
version = projectVersion ?: "LOCAL"

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot"
}

val sonatypeUsername: String? by project
val sonatypePassword: String? by project

repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

val pluginUrl = "https://github.com/IntershopCommunicationsAG/${project.name}"
gradlePlugin {
    website.set(pluginUrl)
    vcsUrl.set(pluginUrl)
    plugins {
        create("ArchitectureReportPlugin") {
            id = "com.intershop.gradle.architectural.report"
            implementationClass = "com.intershop.tool.architecture.report.plugin.ArchitectureReportPlugin"
            displayName = project.displayName
            description = project.description
            tags.set(listOf("intershop", "validation", "analysis"))
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot"
}

jacoco {
    toolVersion = "0.8.10"
}

testing {
    suites.withType<JvmTestSuite> {
        useJUnitJupiter()
        dependencies {
            runtimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
            implementation("org.junit.jupiter:junit-jupiter:5.10.2")
            implementation("org.hamcrest:hamcrest:2.2")
            implementation("com.google.jimfs:jimfs:1.3.0")
            implementation("com.squareup.okhttp3:mockwebserver:4.12.0")
        }
    }
}

tasks {
    register("generateResources") {
        // Generate properties file with plugin version to access this information in plugin itself
        val versionPropertyFile = project.layout.buildDirectory.file("generated/version.properties")
        outputs.file(versionPropertyFile)
        doLast {
            mkdir(versionPropertyFile.get().asFile.parentFile)
            versionPropertyFile.get().asFile.writeText("version=${project.version}")
        }
    }

    withType<ProcessResources> {
        from(files(getTasksByName("generateResources", false)))
    }

    withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)

            html.outputLocation.set(project.layout.buildDirectory.dir("jacocoHtml"))
        }

        val jacocoTestReport by tasks
        jacocoTestReport.dependsOn("test")
    }

    withType<Sign> {
        val sign = this
        withType<PublishToMavenLocal> {
            this.dependsOn(sign)
        }
        withType<PublishToMavenRepository> {
            this.dependsOn(sign)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("intershopMvn") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https:/github.com/IntershopCommunicationsAG/${project.name}")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                organization {
                    name.set("Intershop Communications AG")
                    url.set("https://intershop.com")
                }

                developers {
                    developer {
                        id.set("lead")
                        name.set("David B.")
                        email.set("davidb@intershop.de")
                    }
                }

                scm {
                    connection.set(pluginUrl)
                    developerConnection.set("git@github.com:IntershopCommunicationsAG/${project.name}.git")
                    url.set(pluginUrl)
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

signing {
    sign(publishing.publications["intershopMvn"])
}

dependencies {
    implementation(gradleApi())

    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("org.ow2.asm:asm:9.6")
    implementation("javax.inject:javax.inject:1")
    implementation("commons-io:commons-io:2.15.1")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
    implementation("com.intershop.gradle.icm:icm-gradle-plugin:5.8.0")

    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")

}
