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
    id("base")
    id("java")
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.6.21"

    // test coverage
    id("jacoco")

    // ide plugin
    id("idea")
    id("eclipse")

    // artifact signing - necessary on Maven Central
    id("signing")

    // plugin for publishing to Gradle Portal
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.21.0"
}

// release configuration
group = "com.intershop.gradle.architectural.report"
description = "Gradle architectural report plugin"

// adapt external loading at ArchitectureReportPlugin and README.md too
// IMPORTANT version referenced at com.intershop.tool.architecture.report.plugin.ArchitectureReportPlugin
version = "3.0.0-LOCAL"
// IMPORTANT version referenced at com.intershop.tool.architecture.report.plugin.ArchitectureReportPlugin

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot"
}

java {
    withSourcesJar()
    withJavadocJar()
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

jacoco {
    toolVersion = "0.8.7"
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<JacocoReport> {
        reports {
            xml.required.set(true) // coveralls plugin depends on xml format report
            html.required.set(true)
        }

        val jacocoTestReport by tasks
        jacocoTestReport.dependsOn("test")
    }

    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
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

                scm {
                    connection.set("https://github.com/IntershopCommunicationsAG/${project.name}.git")
                    developerConnection.set("git@github.com:IntershopCommunicationsAG/${project.name}.git")
                    url.set("https://github.com/IntershopCommunicationsAG/${project.name}")
                }
            }
        }
    }
}

pluginBundle {
    website = "https://github.com/IntershopCommunicationsAG/${project.name}"
    vcsUrl = "https://github.com/IntershopCommunicationsAG/${project.name}"
    description = project.description
    tags = listOf("intershop", "gradle", "plugin", "validation", "analysis")
}

gradlePlugin {
    plugins {
        register("ArchitectureReportPlugin") {
            id = "com.intershop.gradle.architectural.report"
            implementationClass = "com.intershop.tool.architecture.report.plugin.ArchitectureReportPlugin"
            displayName = project.description
        }
    }
}

signing {
    sign(publishing.publications["intershopMvn"])
}

dependencies {
    implementation(gradleApi())

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.ow2.asm:asm:9.3")
    implementation("javax.inject:javax.inject:1")
    implementation("commons-io:commons-io:2.11.0")
    implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:3.0.2")
    implementation("com.intershop.gradle.icm:icm-gradle-plugin:5.4.2")

    runtimeOnly("org.apache.cxf:cxf-rt-rs-client:3.5.2")
    runtimeOnly("org.apache.cxf:cxf-rt-transports-http:3.5.2")
    runtimeOnly("org.apache.cxf:cxf-rt-transports-local:3.5.2")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.8.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("com.intershop.gradle.test:test-gradle-plugin:4.1.1")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}
