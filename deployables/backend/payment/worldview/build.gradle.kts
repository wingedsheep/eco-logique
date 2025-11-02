plugins {
    id("economique.kotlin-common")
}

dependencies {
    api(project(":deployables:backend:payment:payment-api"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}
