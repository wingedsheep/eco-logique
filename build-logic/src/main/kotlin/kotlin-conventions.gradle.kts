plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    jacoco
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

dependencies {
    // Modules using this convention import Spring Boot BOM via dependencyManagement,
    // which provides version-managed test dependencies (no versions needed here).
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
