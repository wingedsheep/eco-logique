import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    jacoco
}

extensions.configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

extensions.configure<KotlinJvmProjectExtension> {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}

extensions.configure<JacocoPluginExtension> {
    toolVersion = "0.8.11"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

dependencies {
    // Modules using this convention import Spring Boot BOM via dependencyManagement,
    // which provides version-managed test dependencies (no versions needed here).
    "testImplementation"(kotlin("test"))
    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testImplementation"("org.springframework.boot:spring-boot-starter-webmvc-test")
    "testImplementation"("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    "testImplementation"("org.springframework.boot:spring-boot-starter-jdbc-test")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}
