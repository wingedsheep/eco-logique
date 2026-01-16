plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.2.0")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.0.0")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.7")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.4.1.3373")
}