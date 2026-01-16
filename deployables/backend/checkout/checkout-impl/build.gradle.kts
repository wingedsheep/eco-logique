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
    implementation(project(":checkout:checkout-api"))
    implementation(project(":cart:cart-api"))
    implementation(project(":orders:orders-api"))
    implementation(project(":inventory:inventory-api"))
    implementation(project(":payment:payment-api"))
    implementation(project(":users:users-api"))
    implementation(project(":common:common-money"))
    implementation(project(":common:common-result"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.springdoc.openapi)
    implementation(libs.spring.context)
    implementation(libs.spring.tx)
    implementation(libs.kotlin.reflect)
}
