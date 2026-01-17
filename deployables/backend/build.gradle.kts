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

val coverageExcludedProjects = setOf(":application")

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated code coverage report for module tests"

    val includedProjects = subprojects.filter { it.path !in coverageExcludedProjects }

    val testTasks = includedProjects.flatMap { subproject ->
        subproject.tasks.withType<Test>()
    }
    dependsOn(testTasks)

    val sourceSets = includedProjects.mapNotNull { subproject ->
        subproject.extensions.findByType<SourceSetContainer>()?.findByName("main")
    }

    sourceDirectories.setFrom(sourceSets.flatMap { it.allSource.srcDirs })
    classDirectories.setFrom(sourceSets.map { it.output })

    executionData.setFrom(
        includedProjects.flatMap { subproject ->
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
