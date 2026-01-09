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
    api(project(":cart:cart-api"))
    api(project(":products:products-worldview"))
    api(project(":users:users-worldview"))

    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework:spring-context")
}
