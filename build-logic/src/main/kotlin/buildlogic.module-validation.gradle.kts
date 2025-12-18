// Documentation: docs/development/gradle/module-validation.md

interface ModuleDependencyValidationExtension {
    val allowedDependencies: MapProperty<String, Set<String>>
    val excludedModules: SetProperty<String>
    val failOnViolation: Property<Boolean>
    val reportFile: RegularFileProperty
}

val moduleDependencyValidation = extensions.create<ModuleDependencyValidationExtension>("moduleDependencyValidation")

fun extractModuleName(projectPath: String): String? {
    val segments = projectPath.removePrefix(":").split(":")
    if (segments.isEmpty()) return null

    val lastSegment = segments.last()
    val suffixes = listOf("-api", "-impl", "-worldview")

    for (suffix in suffixes) {
        if (lastSegment.endsWith(suffix)) {
            return lastSegment.removeSuffix(suffix)
        }
    }

    val knownSubmodules = listOf("api", "impl", "worldview")
    if (lastSegment in knownSubmodules) {
        return segments.getOrNull(segments.size - 2)
    }

    return null
}

fun normalizeConfigName(configName: String): String {
    return when {
        configName.startsWith("testFixtures") && (configName.contains("Api") || configName == "testFixturesApi") -> "testFixturesApi"
        configName.startsWith("testFixtures") -> "testFixturesImplementation"
        configName.startsWith("test") && (configName.contains("Api") || configName == "testApi") -> "testApi"
        configName.startsWith("test") -> "testImplementation"
        configName.contains("Api") || configName == "api" || configName == "apiElements" -> "api"
        else -> "implementation"
    }
}

fun buildMarkdownReport(violations: List<String>): String {
    val implToImplViolations = violations.filter { it.contains("use '") }
    val apiToImplViolations = violations.filter { it.contains("api modules cannot") }
    val moduleWhitelistViolations = violations.filter { it.contains("must not depend on") }

    return buildString {
        appendLine("## ❌ Module Dependency Validation Failed")
        appendLine()
        appendLine("Found ${violations.size} violation(s)")
        appendLine()

        if (implToImplViolations.isNotEmpty()) {
            appendLine("### Impl/Worldview → Impl/Worldview Dependencies")
            appendLine()
            appendLine("Implementation and worldview modules must depend on API modules, not other implementation or worldview modules.")
            appendLine()
            appendLine("| Source | Configuration | Target | Suggested Fix |")
            appendLine("|--------|---------------|--------|---------------|")
            implToImplViolations.forEach { violation ->
                val match = Regex("'([^']+)' \\[([^]]+)] depends on '([^']+)' - use '([^']+)'").find(violation)
                if (match != null) {
                    val (source, config, target, fix) = match.destructured
                    appendLine("| `$source` | `$config` | `$target` | `$fix` |")
                } else {
                    appendLine("- $violation")
                }
            }
            appendLine()
        }

        if (apiToImplViolations.isNotEmpty()) {
            appendLine("### API → Impl/Worldview Dependencies")
            appendLine()
            appendLine("API modules cannot depend on implementation or worldview modules.")
            appendLine()
            appendLine("| Source | Configuration | Target |")
            appendLine("|--------|---------------|--------|")
            apiToImplViolations.forEach { violation ->
                val match = Regex("'([^']+)' \\[([^]]+)] depends on '([^']+)'").find(violation)
                if (match != null) {
                    val (source, config, target) = match.destructured
                    appendLine("| `$source` | `$config` | `$target` |")
                } else {
                    appendLine("- $violation")
                }
            }
            appendLine()
        }

        if (moduleWhitelistViolations.isNotEmpty()) {
            appendLine("### Module Whitelist Violations")
            appendLine()
            appendLine("These dependencies are not allowed by the configured module dependency rules.")
            appendLine()
            appendLine("| Source Module | Configuration | Target Module | Details |")
            appendLine("|---------------|---------------|---------------|---------|")
            moduleWhitelistViolations.forEach { violation ->
                val match = Regex("Module '([^']+)' \\[([^]]+)] must not depend on '([^']+)': (.+)").find(violation)
                if (match != null) {
                    val (source, config, target, details) = match.destructured
                    appendLine("| `$source` | `$config` | `$target` | `$details` |")
                } else {
                    appendLine("- $violation")
                }
            }
            appendLine()
        }

        appendLine("---")
        appendLine("See [module-validation.md](docs/development/gradle/module-validation.md) for details.")
    }
}

tasks.register("validateModuleDependencies") {
    group = "verification"
    description = "Validates module dependency rules"
    notCompatibleWithConfigurationCache("Requires access to project dependency graph")

    doLast {
        val violations = mutableListOf<String>()

        val allowedDeps = moduleDependencyValidation.allowedDependencies.getOrElse(emptyMap())
        val excludedModules = moduleDependencyValidation.excludedModules.getOrElse(emptySet())
        val configuredModules = allowedDeps.keys

        rootProject.allprojects.forEach { proj ->
            val projectPath = proj.path

            if (excludedModules.any { projectPath.startsWith(it) }) return@forEach

            val isApiModule = projectPath.endsWith("-api") || projectPath.endsWith(":api")
            val isImplModule = projectPath.endsWith("-impl") || projectPath.endsWith(":impl")
            val isWorldviewModule = projectPath.endsWith("-worldview") || projectPath.endsWith(":worldview")
            val isConcreteModule = isImplModule || isWorldviewModule

            if (!isApiModule && !isConcreteModule) return@forEach

            val sourceModule = extractModuleName(projectPath)

            proj.configurations
                .filter { it.isCanBeResolved }
                .flatMap { config ->
                    config.allDependencies.filterIsInstance<ProjectDependency>().map { config.name to it }
                }
                .forEach depLoop@{ (configName, dep) ->
                    val depPath = dep.dependencyProject.path
                    val normalizedConfig = normalizeConfigName(configName)
                    val isTestConfig = normalizedConfig == "testImplementation" || normalizedConfig == "testApi"
                    val isTestFixturesDep = dep.requestedCapabilities.any { it.name.endsWith("-test-fixtures") }
                    val isTestAllowedDep = isTestConfig && isTestFixturesDep

                    val depIsImpl = depPath.endsWith("-impl") || depPath.endsWith(":impl")
                    val depIsWorldview = depPath.endsWith("-worldview") || depPath.endsWith(":worldview")
                    val depIsConcrete = depIsImpl || depIsWorldview
                    val targetModule = extractModuleName(depPath)

                    if (depPath == projectPath) return@depLoop

                    if (isApiModule && depIsConcrete) {
                        violations.add("'$projectPath' [$normalizedConfig] depends on '$depPath' - api modules cannot depend on impl/worldview modules")
                    }

                    val isWorldviewToWorldview = isWorldviewModule && depIsWorldview
                    if (isConcreteModule && depIsConcrete && sourceModule != targetModule && !isTestAllowedDep && !isWorldviewToWorldview) {
                        val apiPath = depPath
                            .replace("-impl", "-api")
                            .replace(":impl", ":api")
                            .replace("-worldview", "-api")
                            .replace(":worldview", ":api")
                        violations.add("'$projectPath' [$normalizedConfig] depends on '$depPath' - use '$apiPath' instead")
                    }

                    if (sourceModule != null && targetModule != null &&
                        sourceModule != targetModule &&
                        configuredModules.isNotEmpty() &&
                        sourceModule in configuredModules &&
                        targetModule in configuredModules
                    ) {
                        val allowed = allowedDeps[sourceModule] ?: emptySet()

                        if (targetModule !in allowed) {
                            violations.add("Module '$sourceModule' [$normalizedConfig] must not depend on '$targetModule': $projectPath -> $depPath")
                        }
                    }
                }
        }

        val uniqueViolations = violations.distinct()
        val reportFile = moduleDependencyValidation.reportFile.orNull?.asFile

        if (uniqueViolations.isNotEmpty()) {
            val message = "Module dependency violations:\n${uniqueViolations.joinToString("\n") { "  - $it" }}"

            reportFile?.let { file ->
                file.parentFile.mkdirs()
                file.writeText(buildMarkdownReport(uniqueViolations))
                logger.lifecycle("Module validation report written to: ${file.absolutePath}")
            }

            if (moduleDependencyValidation.failOnViolation.getOrElse(true)) {
                throw GradleException(message)
            } else {
                logger.warn(message)
            }
        } else {
            reportFile?.let { file ->
                file.parentFile.mkdirs()
                file.writeText("## ✅ Module Dependency Validation Passed\n\nNo violations found.")
            }
            logger.lifecycle("Module dependency validation passed")
        }
    }
}

tasks.findByName("check")?.dependsOn("validateModuleDependencies")
    ?: tasks.register("check") {
        group = "verification"
        dependsOn("validateModuleDependencies")
    }
