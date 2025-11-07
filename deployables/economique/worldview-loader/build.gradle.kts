plugins {
    id("economique.kotlin-common")
}

dependencies {
    // All domain implementation modules to access repositories
    implementation(project(":deployables:economique:payment"))
    implementation(project(":deployables:economique:products"))
    implementation(project(":deployables:economique:shipping"))
    implementation(project(":deployables:economique:inventory"))
    implementation(project(":deployables:economique:users"))

    // Spring Boot
    implementation(libs.bundles.spring.boot)
}