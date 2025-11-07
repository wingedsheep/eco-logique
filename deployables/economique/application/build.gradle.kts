plugins {
    id("economique.spring-boot")
}

dependencies {
    // All bounded context modules
    implementation(project(":deployables:economique:payment"))
    implementation(project(":deployables:economique:products"))
    implementation(project(":deployables:economique:shipping"))
    implementation(project(":deployables:economique:inventory"))
    implementation(project(":deployables:economique:users"))

    // Worldview data loader (loads test data in non-prod environments)
    implementation(project(":deployables:economique:worldview-loader"))

    // Spring Boot
    implementation(libs.bundles.spring.boot)

    // Flyway for database migrations
    implementation(libs.bundles.flyway)

    // Database
    runtimeOnly(libs.postgresql)

    // Testing
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.awaitility)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}
