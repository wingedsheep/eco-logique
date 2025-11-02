plugins {
    id("economique.spring-boot")
    application
}

dependencies {
    // All domain implementations
    implementation(project(":deployables:backend:payment:payment-impl"))
    implementation(project(":deployables:backend:products:products-impl"))
    implementation(project(":deployables:backend:shipping:shipping-impl"))
    implementation(project(":deployables:backend:inventory:inventory-impl"))
    implementation(project(":deployables:backend:users:users-impl"))

    // All worldviews for dev/test data
    implementation(project(":deployables:backend:payment:payment-worldview"))
    implementation(project(":deployables:backend:products:products-worldview"))
    implementation(project(":deployables:backend:shipping:shipping-worldview"))
    implementation(project(":deployables:backend:inventory:inventory-worldview"))
    implementation(project(":deployables:backend:users:users-worldview"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")

}

application {
    mainClass.set("com.wingedsheep.ecologique.EcologiqueApplicationKt")
}
