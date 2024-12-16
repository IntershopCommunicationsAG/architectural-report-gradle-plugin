plugins {
    id("com.gradle.develocity") version "3.19"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
    }
}

rootProject.name = "architectural-report-gradle-plugin"
