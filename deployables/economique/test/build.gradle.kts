plugins {
    id("economique.kotlin-common")
}

dependencies {
    // Application for E2E tests
    testImplementation(project(":deployables:economique:application"))

    // Test fixtures from all contexts
    testImplementation(testFixtures(project(":deployables:economique:payment")))
    testImplementation(testFixtures(project(":deployables:economique:products")))
    testImplementation(testFixtures(project(":deployables:economique:shipping")))
    testImplementation(testFixtures(project(":deployables:economique:inventory")))
    testImplementation(testFixtures(project(":deployables:economique:users")))

    // Spring Boot for testing
    testImplementation(libs.spring.boot.starter.test)

    // Testing dependencies
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.awaitility)
}
