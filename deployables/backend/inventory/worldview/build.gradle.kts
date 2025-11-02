plugins {
    id("economique.kotlin-common")
}

dependencies {
    api(project(":deployables:backend:inventory:inventory-api"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}
