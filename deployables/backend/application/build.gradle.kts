plugins {
    id("kotlin-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    application
}

group = "com.wingedsheep"
version = "0.0.1-SNAPSHOT"

tasks.jacocoTestReport {
    enabled = false
}

dependencies {
    implementation(project(":products:products-impl"))
    implementation(project(":products:products-worldview"))
    implementation(project(":users:users-impl"))
    implementation(project(":users:users-worldview"))
    implementation(project(":orders:orders-impl"))
    implementation(project(":orders:orders-worldview"))
    implementation(project(":cart:cart-impl"))
    implementation(project(":cart:cart-worldview"))
    implementation(project(":email:email-impl"))
    implementation(project(":payment:payment-api"))
    implementation(project(":payment:payment-impl"))
    implementation(project(":inventory:inventory-api"))
    implementation(project(":inventory:inventory-impl"))
    implementation(project(":inventory:inventory-worldview"))
    implementation(project(":checkout:checkout-impl"))
    implementation(project(":shipping:shipping-api"))
    implementation(project(":shipping:shipping-impl"))
    implementation(project(":shipping:shipping-worldview"))
    implementation(project(":common:common-outbox"))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.jackson2.module.kotlin)
    implementation(libs.jackson2.datatype.jsr310)

    implementation(libs.springdoc.openapi)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    runtimeOnly(libs.postgresql)

    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.cucumber.java)
    testImplementation(libs.cucumber.junit.platform)
    testImplementation(libs.cucumber.spring)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.rabbitmq)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.spring.boot.starter.webflux)
    testImplementation(testFixtures(project(":products:products-api")))
    testImplementation(testFixtures(project(":orders:orders-api")))
    testImplementation(testFixtures(project(":cart:cart-api")))
    testImplementation(project(":checkout:checkout-api"))
    testImplementation(project(":cart:cart-api"))
    testImplementation(libs.mockito.kotlin)
}

application {
    mainClass.set("com.wingedsheep.ecologique.EcologiqueApplicationKt")
}
