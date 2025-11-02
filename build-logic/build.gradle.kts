plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.0")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.4")
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.0")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.4.1.3373")
}
