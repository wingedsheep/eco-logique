plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    id("buildlogic.module-validation")
    jacoco
}

moduleDependencyValidation {
    failOnViolation.set(true)
    excludedModules.set(setOf(":application"))
    compositionRoots.set(setOf(":application"))  // No module may depend on these
    reportFile.set(layout.buildDirectory.file("reports/module-validation/report.md"))
    allowedDependencies.set(
        mapOf(
            "products" to emptySet(),
            "users" to setOf("email"),
            "orders" to setOf("products", "users", "inventory", "payment", "shipping"),
            "cart" to setOf("products", "users"),
            "email" to emptySet(),
            "inventory" to setOf("products"),
            "payment" to emptySet(),
            "shipping" to setOf("orders", "inventory", "payment"),
        )
    )
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated code coverage report for module tests"

    val excludedProjectPaths = setOf(":application")

    // Depend on jacocoTestReport tasks from impl modules (they have JaCoCo enabled via kotlin-conventions)
    dependsOn(
        subprojects
            .filter { it.path !in excludedProjectPaths && it.path.endsWith("-impl") }
            .map { "${it.path}:jacocoTestReport" }
    )

    // Source directories for all modules (excluding application)
    val sourceFiles = subprojects
        .filter { it.path !in excludedProjectPaths }
        .mapNotNull { subproject ->
            subproject.extensions.findByType<SourceSetContainer>()
                ?.findByName("main")
                ?.allSource
                ?.srcDirs
        }
        .flatten()
    sourceDirectories.setFrom(sourceFiles)

    // Compiled class files for all modules (excluding application)
    val classFiles = subprojects
        .filter { it.path !in excludedProjectPaths }
        .mapNotNull { subproject ->
            subproject.extensions.findByType<SourceSetContainer>()
                ?.findByName("main")
                ?.output
                ?.classesDirs
        }
    classDirectories.setFrom(classFiles)

    // Collect all JaCoCo execution data files (excluding application module)
    executionData.setFrom(
        subprojects
            .filter { it.path !in excludedProjectPaths }
            .map { subproject ->
                fileTree(subproject.layout.buildDirectory) {
                    include("jacoco/*.exec")
                }
            }
    )

    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated-html"))
    }
}
