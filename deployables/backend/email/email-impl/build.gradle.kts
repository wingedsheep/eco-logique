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
    implementation(project(":email:email-api"))

    implementation(libs.spring.context)
    implementation(libs.kotlin.reflect)
}
