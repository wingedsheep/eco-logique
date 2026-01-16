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
    implementation(project(":inventory:inventory-api"))
    implementation(project(":common:common-result"))

    implementation(libs.spring.context)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.tx)
    implementation(libs.kotlin.reflect)
}
