plugins {
    id("kotlin-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    application
}

group = "com.wingedsheep"
version = "0.0.1-SNAPSHOT"

tasks.jacocoTestReport {
    enabled = false
}

dependencies {
    implementation(project(":products:products-impl"))
    implementation(project(":products:products-worldview"))
    implementation(project(":users:users-impl"))
    implementation(project(":users:users-worldview"))
    implementation(project(":orders:orders-impl"))
    implementation(project(":orders:orders-worldview"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    implementation(libs.springdoc.openapi)

    implementation(libs.flyway.core)
    runtimeOnly(libs.postgresql)

    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.cucumber.java)
    testImplementation(libs.cucumber.junit.platform)
    testImplementation(libs.cucumber.spring)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.rest.assured)
    testImplementation(testFixtures(project(":products:products-api")))
    testImplementation(testFixtures(project(":orders:orders-api")))
    testImplementation(libs.mockito.kotlin)
}

application {
    mainClass.set("com.wingedsheep.ecologique.EcologiqueApplicationKt")
}
