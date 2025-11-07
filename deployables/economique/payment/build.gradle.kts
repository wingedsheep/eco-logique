plugins {
    id("economique.kotlin-library")
    id("economique.test-fixtures")
}

dependencies {
    // Common modules
    api(project(":common:common-money"))

    // Spring Boot
    implementation(libs.bundles.spring.boot)

    // Database
    runtimeOnly(libs.postgresql)

    // Testing
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.awaitility)
}