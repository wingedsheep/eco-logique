plugins {
    id("kotlin-conventions")
    alias(libs.plugins.spring.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

dependencies {
    api(project(":users:users-api"))

    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.context)
}
