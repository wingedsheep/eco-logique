plugins {
    id("economique.kotlin-common")
    id("economique.spring-boot")
}

dependencies {
    api(project(":deployables:backend:inventory:inventory-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
