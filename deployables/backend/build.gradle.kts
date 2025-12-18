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
    reportFile.set(layout.buildDirectory.file("reports/module-validation/report.md"))
    allowedDependencies.set(
        mapOf(
            "products" to emptySet(),
            "users" to emptySet(),
            "orders" to setOf("products", "users"),
        )
    )
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated code coverage report"

    val testTasks = subprojects.flatMap { subproject ->
        subproject.tasks.withType<Test>()
    }
    dependsOn(testTasks)

    val sourceSets = subprojects.mapNotNull { subproject ->
        subproject.extensions.findByType<SourceSetContainer>()?.findByName("main")
    }

    sourceDirectories.setFrom(sourceSets.flatMap { it.allSource.srcDirs })
    classDirectories.setFrom(sourceSets.map { it.output })

    executionData.setFrom(
        fileTree(layout.projectDirectory) {
            include("**/build/jacoco/*.exec")
        }
    )

    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated-html"))
    }
}
