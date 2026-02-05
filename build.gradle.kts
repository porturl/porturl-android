
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property("sonar.projectKey", "porturl_porturl-android")
        property("sonar.organization", "porturl")
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.projectDir}/app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}
