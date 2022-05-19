[![Build Status](https://github.com/IntershopCommunicationsAG/architectural-report-gradle-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/IntershopCommunicationsAG/architectural-report-gradle-plugin/actions/workflows/build.yml)

# Introduction

This project provides a tool to identify architectural mistakes.

# Usage

The plugin can be applied to all Gradle Intershop projects.
<details>
    <summary>Groovy</summary>

In build.gradle:
```groovy
plugins {
    id "com.intershop.gradle.architectural.report" version "3.0.0"
}

architectureReport {
    keySelector = ["com.intershop.java.api.violation"]
    baselineFile = file("api_definition_baseline.xml")
    knownIssuesFile = file("known_issues.xml")
}
```
</details>
<details>
    <summary>Kotlin</summary>

In build.gradle.kts:
```kotlin
plugins {
    id("com.intershop.gradle.architectural.report") version "3.0.0"
}

architectureReport {
    keySelector.set(listOf("com.intershop.java.api.violation"))
    baselineFile.set(file("api_definition_baseline.xml"))
    knownIssuesFile.set(file("known_issues.xml"))
}
```
</details>

Start the architecture report task:
```bash
./gradlew validateArchitecture
```

# Tasks

| Task                 | Description                                                      |
|----------------------|------------------------------------------------------------------|
| validateArchitecture | Task creates API definition, API diffs, list of collected issues |

# Validation Keys

| Key                              | Description                                           |
|----------------------------------|-------------------------------------------------------|
| com.intershop.java.api.violation | API differences to an API baseline                    |
| com.intershop.java.capi.internal | Validate that CAPI doesn't use internal classes       |
| com.intershop.library.update     | New version contains major third party library update |
| com.intershop.library.new        | New version contains a new third party library        |

<!--
# Disabled Validation Keys
| Key                                        | Description                                                                  |
|--------------------------------------------|------------------------------------------------------------------------------|
| com.intershop.isml.xss                     | Possible XSS issues in isml (Intershop Markup (Template) Language) templates |
| com.intershop.businessobject.persistence   | References to persistence layer inside of business object API                |
| com.intershop.businessobject.internal      | References to internal classes inside of business object API                 |
| com.intershop.pipelet.unused               | Pipelet can be removed / no longer used                                     |
| com.intershop.pipelet.used.deprecated      | Pipelet is deprecated, but still in use by pipelines                         |
| com.intershop.pipeline.invalid.pipelineref | Pipeline contains references to non existing pipeline start nodes            |
-->

# Configuration

| Key                    | Type                       | Default value                                                                                                                                      | Description                                                                                                                                                                                                                   |
|------------------------|----------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| keySelector            | ListProperty&lt;String&gt; |                                                                                                                                                    | Define keys for validation, other issues will be ignored                                                                                                                                                                      |
| dependenciesFile       | RegularFileProperty        | `<project.buildDir>/architectureReport/dependencies.txt` <br> (All transitive Gradle project dependencies like libraries, cartridges will be used) | (optional) Specifies dependencies TXT-file whereas each line represents a dependency in format: <ul><li>`self:group:module:version`</li><li>`library:group:module:version`</li><li>`cartridge:group:module:version`</li></ul> |
| knownIssuesFile        | RegularFileProperty        |                                                                                                                                                    | (optional) Read known issues from a XML-file, listed issues will be ignored                                                                                                                                                   |
| baselineFile           | RegularFileProperty        |                                                                                                                                                    | (optional) API baseline (previously published api_definition.xml of baseline release)                                                                                                                                         |
| useExternalProcess     | Property&lt;Boolean&gt;    | `true`                                                                                                                                             | (optional) Whether to start architecture report tool in external Java process                                                                                                                                                 |
| additionalJvmArguments | ListProperty&lt;String&gt; |                                                                                                                                                    | (optional) Additional JVM arguments                                                                                                                                                                                           |
| reportsDirectory       | DirectoryProperty          | `<project.buildDir>/architectureReport`                                                                                                            | (optional) Directory to write reports (new_issues, api_definition, resolved_issues)                                                                                                                                           |

# Output

| File               | Description                                                            |
|--------------------|------------------------------------------------------------------------|
| api_definition.xml | API extraction of current build                                        |
| api_violation.xml  | Removed API entries (compared to document located at 'baseLineURI')    |
| fixed_issues.xml   | Issues, which are listed at 'knownIssuesFile', but not longer detected |
| new_issues.xml     | Issues, which are not listed at 'knownIssuesFile', but detected        |

# License

Copyright 2014-2022 Intershop Communications.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
