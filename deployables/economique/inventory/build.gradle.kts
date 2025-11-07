plugins {
    id("economique.kotlin-library")
    id("economique.test-fixtures")
}

dependencies {
    // Common modules
    api(project(":common:common-country"))

    // Other bounded contexts
    implementation(project(":deployables:economique:products"))

    // Spring Boot
    implementation(libs.bundles.spring.boot)

    // Database
    runtimeOnly(libs.postgresql)

    // Testing
    testImplementation(testFixtures(project(":deployables:economique:products")))
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.awaitility)
}