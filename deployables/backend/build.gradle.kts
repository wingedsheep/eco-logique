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

    // Include all leaf modules with source sets, except application
    val includedProjects = subprojects.filter { subproject ->
        subproject.path != ":application" &&
            subproject.extensions.findByType<SourceSetContainer>()?.findByName("main") != null
    }

    dependsOn(includedProjects.map { "${it.path}:jacocoTestReport" })

    sourceDirectories.setFrom(includedProjects.map {
        it.extensions.getByType<SourceSetContainer>().getByName("main").allSource.srcDirs
    }.flatten())

    classDirectories.setFrom(includedProjects.map {
        it.extensions.getByType<SourceSetContainer>().getByName("main").output.classesDirs
    })

    executionData.setFrom(includedProjects.map {
        fileTree(it.layout.buildDirectory) { include("jacoco/*.exec") }
    })

    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated-html"))
    }
}
