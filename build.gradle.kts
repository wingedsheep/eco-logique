plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("buildlogic.module-validation")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

moduleDependencyValidation {
    failOnViolation.set(true)
    excludedModules.set(setOf(":deployables:backend:application", ":test"))
    reportFile.set(layout.buildDirectory.file("reports/module-validation/report.md"))
    allowedDependencies.set(
        mapOf(
            "products" to emptySet()
        )
    )
}
