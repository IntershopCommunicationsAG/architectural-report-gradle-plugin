plugins {
    id("com.gradle.develocity") version "4.6.0"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
    }
}

rootProject.name = "architectural-report-gradle-plugin"
