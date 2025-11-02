plugins {
    id("economique.kotlin-common")
}

dependencies {
    api(project(":deployables:backend:users:users-api"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}
