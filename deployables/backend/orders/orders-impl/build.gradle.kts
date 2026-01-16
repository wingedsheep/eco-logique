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
    implementation(project(":orders:orders-api"))
    implementation(project(":products:products-api"))
    implementation(project(":inventory:inventory-api"))
    implementation(project(":payment:payment-api"))
    implementation(project(":shipping:shipping-api"))
    implementation(project(":common:common-money"))
    implementation(project(":common:common-result"))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.context)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.springdoc.openapi)

    testImplementation(testFixtures(project(":orders:orders-api")))
    testImplementation(testFixtures(project(":products:products-api")))
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.assertj.core)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.cucumber.java)
    testImplementation(libs.cucumber.junit.platform)
    testImplementation(libs.cucumber.spring)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.spring.boot.starter.webflux)
    testImplementation(libs.spring.boot.starter.flyway)
    testImplementation(libs.flyway.database.postgresql)
    testRuntimeOnly(libs.postgresql)
}
