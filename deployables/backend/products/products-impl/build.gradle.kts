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
    implementation(project(":products:products-api"))
    implementation(project(":common:common-money"))
    implementation(project(":common:common-result"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework:spring-context")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(testFixtures(project(":products:products-api")))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.testcontainers:postgresql:1.19.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.1")
    testImplementation("io.cucumber:cucumber-java:7.14.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.14.0")
    testImplementation("io.cucumber:cucumber-spring:7.14.0")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.1")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation("org.flywaydb:flyway-core")
    testRuntimeOnly("org.postgresql:postgresql")
}
