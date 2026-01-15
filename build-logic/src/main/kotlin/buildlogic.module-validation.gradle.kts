// Documentation: docs/development/gradle/module-validation.md

import buildlogic.ValidateModuleDependenciesTask

interface ModuleDependencyValidationExtension {
    val allowedDependencies: MapProperty<String, Set<String>>
    val excludedModules: SetProperty<String>
    val compositionRoots: SetProperty<String>
    val failOnViolation: Property<Boolean>
    val reportFile: RegularFileProperty
}

val moduleDependencyValidation = extensions.create<ModuleDependencyValidationExtension>("moduleDependencyValidation")

tasks.register<ValidateModuleDependenciesTask>("validateModuleDependencies") {
    group = "verification"
    description = "Validates module dependency rules"
    notCompatibleWithConfigurationCache("Requires access to project dependency graph")

    failOnViolation.set(moduleDependencyValidation.failOnViolation.orElse(true))
    reportFile.set(moduleDependencyValidation.reportFile)
    allowedDependencies.set(moduleDependencyValidation.allowedDependencies.orElse(emptyMap()))
    excludedModules.set(moduleDependencyValidation.excludedModules.orElse(emptySet()))
    compositionRoots.set(moduleDependencyValidation.compositionRoots.orElse(emptySet()))

    projectsProvider.set(provider { rootProject.allprojects })
}

tasks.findByName("check")?.dependsOn("validateModuleDependencies")
    ?: tasks.register("check") {
        group = "verification"
        dependsOn("validateModuleDependencies")
    }

