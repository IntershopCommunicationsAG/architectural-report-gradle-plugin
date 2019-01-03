[![Build Status](https://travis-ci.org/IntershopCommunicationsAG/architectural-report-gradle-plugin.svg?branch=master)](https://travis-ci.org/IntershopCommunicationsAG/architectural-report-gradle-plugin)

# Introduction

This project provides a tool to identify architectural mistakes.

# Usage

The plugin can be registered in all intershop assemblies.

Include to build.gradle
<pre>
buildscript {
    dependencies {
        classpath 'com.intershop.gradle.architectural.report:architectural-report-gradle-plugin:1.1.4-rc1'
    }
}
apply plugin: 'com.intershop.gradle.architectural.report'

architectureReport {
    ivyFile= new File (deployServer.targetDirectory, 'share/ivy.xml')
    cartridgesDir = new File (deployServer.targetDirectory, '/share/system/cartridges')
    baselineFile = new File (rootProject.projectDir, 'api_definition_baseline.xml')
    knownIssuesFile = new File(rootProject.projectDir, 'known_issues.xml')
    keySelector = ['com.intershop.api.violation']
}
</pre>

Start the architecture report task:
<pre>
./gradlew validateArchitecture
</pre>

# Tasks

| task                 | description                                                                  |
|----------------------|------------------------------------------------------------------------------|
| validateArchitecture | task creates api definition, api diffs, list of collected issues             |

# Validation Keys

| key                                        | description                                                                  |
|--------------------------------------------|------------------------------------------------------------------------------|
| com.intershop.api.violation                | api differences to an api baseline                                           |
| com.intershop.isml.xss                     | possible XSS issues in isml (Intershop Markup (Template) Language) templates |
| com.intershop.businessobject.persistence   | references to persistence layer inside of business object api                |
| com.intershop.businessobject.internal      | references to internal classes inside of business object api                 |
| com.intershop.pipelet.unused               | pipelet can be removed / not longer used                                     |
| com.intershop.pipelet.used.deprecated      | pipelet is deprecated, but still in use by pipelines                         |
| com.intershop.pipeline.invalid.pipelineref | pipeline contains references to non existing pipeline start nodes            |
| com.intershop.library.update               | new version contains major third party library update                        |
| com.intershop.library.new                  | new version contains a new third party library                               |

# Configuration

| key             | type         | description                                                                |
|-----------------|--------------|----------------------------------------------------------------------------|
| reportsDir      | File         | write reports (new_issues, api_definition, resolved_issues)                |
| knownIssuesFile | File         | (optional) read known issues from a xml-file, listed issues will be ignored           |
| keySelector     | List&lt;String&gt; | define keys for validation, other issues will be ignored                        |
| baselineFile    | File         | (optional) api baseline (previously published api_definition.xml of baseline release) |
| cartridgesDir   | File         | cartridge directory of ICM server                                          |
| ivyFile         | File         | ivy.xml file contains server dependencies and build numbers                |

# Output

| file               | description                                                            |
|--------------------|------------------------------------------------------------------------|
| api_definition.xml | api extraction of current build                                        |
| api_violation.xml  | removed api entries (compared to document located at 'baseLineURI')    |
| fixed_issues.xml   | issues, which are listed at 'knownIssuesFile', but not longer detected |
| new_issues.xml     | issues, which are not listed at 'knownIssuesFile', but detected        |

# License

Copyright 2014-2016 Intershop Communications.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
