plugins {
    id("com.gradle.enterprise") version "3.18"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

rootProject.name = "architectural-report-gradle-plugin"
