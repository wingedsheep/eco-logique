plugins {
    id("economique.kotlin-common")
}

dependencies {
    api(project(":deployables:backend:shipping:shipping-api"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}
