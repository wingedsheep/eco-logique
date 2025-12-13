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
    api(project(":products:products-api"))
    implementation(project(":common:common-money"))

    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework:spring-context")
}
