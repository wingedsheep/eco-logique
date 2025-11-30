plugins {
    id("kotlin-conventions")
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    application
}

group = "com.wingedsheep"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":deployables:backend:products:products-impl"))
    implementation(project(":deployables:backend:products:products-worldview"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("io.cucumber:cucumber-java:7.14.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.14.0")
    testImplementation("io.cucumber:cucumber-spring:7.14.0")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.1")
    testImplementation("org.testcontainers:postgresql:1.19.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.1")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation(testFixtures(project(":deployables:backend:products:products-api")))
}

application {
    mainClass.set("com.wingedsheep.ecologique.EcologiqueApplicationKt")
}
