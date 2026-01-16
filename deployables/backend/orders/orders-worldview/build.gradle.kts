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
    api(project(":orders:orders-api"))
    implementation(project(":products:products-api"))
    implementation(project(":users:users-worldview"))
    implementation(project(":common:common-money"))

    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.context)
}
