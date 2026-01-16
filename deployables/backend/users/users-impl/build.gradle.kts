plugins {
    id("kotlin-conventions")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot) apply false
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

dependencies {
    implementation(project(":users:users-api"))
    implementation(project(":email:email-api"))
    implementation(project(":common:common-country"))
    implementation(project(":common:common-result"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.context)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.springdoc.openapi)

    testImplementation(testFixtures(project(":users:users-api")))
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.assertj.core)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.cucumber.java)
    testImplementation(libs.cucumber.junit.platform)
    testImplementation(libs.cucumber.spring)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.rest.assured)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.database.postgresql)
    testRuntimeOnly(libs.postgresql)
}
