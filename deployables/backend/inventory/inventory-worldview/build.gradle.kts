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
    api(project(":inventory:inventory-api"))
    implementation(project(":common:common-country"))
    implementation(project(":products:products-worldview"))

    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.context)
}
