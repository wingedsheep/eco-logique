plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    id("buildlogic.module-validation")
}

moduleDependencyValidation {
    allowedDependencies.put("products", emptySet())
    failOnViolation.set(true)
    reportFile.set(layout.buildDirectory.file("reports/module-validation/report.md"))
}
