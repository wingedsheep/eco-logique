plugins {
    id("economique.kotlin-common")
}

dependencies {
    api(project(":deployables:backend:products:products-api"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}
