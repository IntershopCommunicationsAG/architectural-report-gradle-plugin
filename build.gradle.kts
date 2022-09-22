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
    // project plugins
    `java-gradle-plugin`
    kotlin("jvm") version "1.7.10"

    // test coverage
    jacoco

    // ide plugin
    idea
    eclipse

    // publish plugin
    `maven-publish`

    // artifact signing - necessary on Maven Central
    signing

    // intershop version plugin
    id("com.intershop.gradle.scmversion") version "6.2.0"

    // plugin for publishing to Gradle Portal
    id("com.gradle.plugin-publish") version "1.0.0"
}

scm {
    version {
        type = "threeDigits"
        initialVersion = "1.0.0"
    }
}

// release configuration
group = "com.intershop.gradle.architectural.report"
description = "Gradle architectural report plugin"

// IMPORTANT version referenced at README.md, adapt it there
version = scm.version.version
// IMPORTANT version referenced at README.md, adapt it there

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot"
}

val sonatypeUsername: String? by project
val sonatypePassword: String? by project

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("ArchitectureReportPlugin") {
            id = "com.intershop.gradle.architectural.report"
            implementationClass = "com.intershop.tool.architecture.report.plugin.ArchitectureReportPlugin"
            displayName = project.displayName
            description = project.description
        }
    }
}

pluginBundle {
    website = "https://github.com/IntershopCommunicationsAG/${project.name}"
    vcsUrl = "https://github.com/IntershopCommunicationsAG/${project.name}"
    description = project.description
    tags = listOf("intershop", "validation", "analysis")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot'"
}

jacoco {
    toolVersion = "0.8.7"
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    register("generateResources") {
        // Generate properties file with plugin version to access this information in plugin itself
        val versionPropertyFile = file("${buildDir}/generated/version.properties")
        outputs.file(versionPropertyFile)
        doLast {
            mkdir(versionPropertyFile.parentFile)
            versionPropertyFile.writeText("version=${project.version}")
        }
    }

    withType<KotlinCompile>  {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }

    withType<ProcessResources> {
        from(files(getTasksByName("generateResources", false)))
    }

    withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)

            html.outputLocation.set( File(project.buildDir, "jacocoHtml") )
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
                    connection.set("https://github.com/IntershopCommunicationsAG/${project.name}.git")
                    developerConnection.set("git@github.com:IntershopCommunicationsAG/${project.name}.git")
                    url.set("https://github.com/IntershopCommunicationsAG/${project.name}")
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

    implementation("org.slf4j:slf4j-api:2.0.1")
    implementation("org.ow2.asm:asm:9.3")
    implementation("javax.inject:javax.inject:1")
    implementation("commons-io:commons-io:2.11.0")
    implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.0")
    implementation("com.intershop.gradle.icm:icm-gradle-plugin:5.4.2")

    runtimeOnly("org.apache.cxf:cxf-rt-rs-client:3.5.3")
    runtimeOnly("org.apache.cxf:cxf-rt-transports-http:3.5.3")
    runtimeOnly("org.apache.cxf:cxf-rt-transports-local:3.5.3")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("com.google.jimfs:jimfs:1.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    //testImplementation("com.intershop.gradle.test:test-gradle-plugin:4.1.2")
}

