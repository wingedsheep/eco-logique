plugins {
    id("economique.kotlin-library")
    id("economique.test-fixtures")
}

dependencies {
    // Common modules
    api(project(":common:common-money"))
    api(project(":common:common-country"))

    // Other bounded contexts (for integration)
    implementation(project(":deployables:economique:products"))
    implementation(project(":deployables:economique:inventory"))

    // Spring Boot
    implementation(libs.bundles.spring.boot)

    // Database
    runtimeOnly(libs.postgresql)

    // Testing
    testImplementation(testFixtures(project(":deployables:economique:products")))
    testImplementation(testFixtures(project(":deployables:economique:inventory")))
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.awaitility)
}