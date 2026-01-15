package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

enum class ModuleType { API, IMPL, WORLDVIEW, LIBRARY }
enum class SourceSet { MAIN, TEST, TEST_FIXTURES }

abstract class ValidateModuleDependenciesTask : DefaultTask() {

    @get:Internal
    abstract val projectsProvider: SetProperty<Project>

    @get:Input
    abstract val allowedDependencies: MapProperty<String, Set<String>>

    @get:Input
    abstract val excludedModules: SetProperty<String>

    @get:Input
    abstract val compositionRoots: SetProperty<String>

    @get:Input
    abstract val failOnViolation: Property<Boolean>

    @get:OutputFile
    @get:Optional
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun validate() {
        val allowed = allowedDependencies.get()
        val excluded = excludedModules.get()
        val roots = compositionRoots.get()

        val violations = projectsProvider.get()
            .flatMap { proj -> collectDependencies(proj) }
            .filter { it.sourceProject != it.targetProject }
            .filterNot { excluded.any { ex -> it.sourceProject.startsWith(ex) } }
            .filter { getModuleType(it.sourceProject) != ModuleType.LIBRARY }
            .mapNotNull { validateDependency(it, allowed, roots) }
            .distinct()

        outputResults(violations)
    }

    private fun collectDependencies(project: Project) = project.configurations.flatMap { config ->
        config.dependencies.filterIsInstance<ProjectDependency>().map { dep ->
            DependencyRecord(
                sourceProject = project.path,
                targetProject = dep.dependencyProject.path,
                configName = config.name,
                isTestFixturesDep = dep.requestedCapabilities.any { it.name.endsWith("-test-fixtures") },
            )
        }
    }

    private fun validateDependency(
        record: DependencyRecord,
        allowedDeps: Map<String, Set<String>>,
        compositionRoots: Set<String>,
    ): String? {
        val (srcPath, tgtPath, configName, isTestFixturesDep) = record

        val sourceType = getModuleType(srcPath)
        val targetType = getModuleType(tgtPath)
        val sourceSet = getSourceSet(configName)
        val srcModule = extractModuleName(srcPath)
        val tgtModule = extractModuleName(tgtPath)
        val isSameModule = srcModule != null && srcModule == tgtModule

        fun suggestedFix() = tgtPath
            .replace("-impl", "-api").replace(":impl", ":api")
            .replace("-worldview", "-api").replace(":worldview", ":api")

        fun checkWhitelist(): String? {
            if (allowedDeps.isEmpty() || srcModule == null || tgtModule == null) return null
            if (srcModule !in allowedDeps) return null
            val allowed = allowedDeps[srcModule] ?: emptySet()
            return if (tgtModule !in allowed) {
                "Module '$srcModule' [$configName] must not depend on '$tgtModule': $srcPath -> $tgtPath"
            } else null
        }

        if (compositionRoots.any { tgtPath.startsWith(it) }) {
            return "'$srcPath' [$configName] depends on '$tgtPath' - cannot depend on composition root"
        }

        if (targetType == ModuleType.LIBRARY) return null

        if (sourceType == ModuleType.API) {
            return when {
                targetType != ModuleType.API ->
                    "'$srcPath' [$configName] depends on '$tgtPath' - api modules can only depend on api modules"
                !isSameModule -> checkWhitelist()
                else -> null
            }
        }

        return when (sourceSet) {
            SourceSet.MAIN -> when {
                isSameModule -> null
                targetType == ModuleType.API -> checkWhitelist()
                targetType == ModuleType.IMPL ->
                    "'$srcPath' [$configName] depends on '$tgtPath' - use '${suggestedFix()}' instead"
                targetType == ModuleType.WORLDVIEW && sourceType == ModuleType.IMPL ->
                    "'$srcPath' [$configName] depends on '$tgtPath' - impl cannot depend on worldview, use '${suggestedFix()}' instead"
                targetType == ModuleType.WORLDVIEW -> checkWhitelist()
                else -> null
            }
            SourceSet.TEST_FIXTURES -> when {
                isSameModule -> null
                targetType == ModuleType.API -> checkWhitelist()
                targetType == ModuleType.IMPL || targetType == ModuleType.WORLDVIEW ->
                    "'$srcPath' [$configName] depends on '$tgtPath' - testFixtures cannot depend on cross-module impl/worldview"
                else -> null
            }
            SourceSet.TEST -> when {
                isSameModule -> null
                targetType == ModuleType.API -> checkWhitelist()
                targetType == ModuleType.IMPL ->
                    "'$srcPath' [$configName] depends on '$tgtPath' - tests cannot depend on cross-module impl (use testFixtures)"
                targetType == ModuleType.WORLDVIEW && !isTestFixturesDep ->
                    "'$srcPath' [$configName] depends on '$tgtPath' - tests cannot depend on cross-module worldview main"
                else -> null
            }
        }
    }

    private fun outputResults(violations: List<String>) {
        val file = reportFile.orNull?.asFile

        if (violations.isNotEmpty()) {
            val message = "Module dependency violations:\n${violations.joinToString("\n") { "  - $it" }}"
            file?.let {
                it.parentFile.mkdirs()
                it.writeText(buildMarkdownReport(violations))
                logger.lifecycle("Module validation report written to: ${it.absolutePath}")
            }
            if (failOnViolation.get()) throw GradleException(message) else logger.warn(message)
        } else {
            file?.let {
                it.parentFile.mkdirs()
                it.writeText("## ✅ Module Dependency Validation Passed\n\nNo violations found.")
            }
            logger.lifecycle("Module dependency validation passed")
        }
    }

    companion object {
        fun extractModuleName(projectPath: String): String? {
            val segments = projectPath.removePrefix(":").split(":")
            if (segments.isEmpty()) return null
            val lastSegment = segments.last()
            listOf("-api", "-impl", "-worldview").forEach { suffix ->
                if (lastSegment.endsWith(suffix)) return lastSegment.removeSuffix(suffix)
            }
            return if (lastSegment in listOf("api", "impl", "worldview")) segments.getOrNull(segments.size - 2) else null
        }

        fun getModuleType(projectPath: String): ModuleType = when {
            projectPath.endsWith("-api") || projectPath.endsWith(":api") -> ModuleType.API
            projectPath.endsWith("-impl") || projectPath.endsWith(":impl") -> ModuleType.IMPL
            projectPath.endsWith("-worldview") || projectPath.endsWith(":worldview") -> ModuleType.WORLDVIEW
            else -> ModuleType.LIBRARY
        }

        fun getSourceSet(configName: String): SourceSet = when {
            configName.startsWith("testFixtures") -> SourceSet.TEST_FIXTURES
            configName.startsWith("test") -> SourceSet.TEST
            else -> SourceSet.MAIN
        }

        private fun buildMarkdownReport(violations: List<String>): String = buildString {
            appendLine("## ❌ Module Dependency Validation Failed")
            appendLine()
            appendLine("Found ${violations.size} violation(s)")
            appendLine()

            violations.groupBy { v ->
                when {
                    v.contains("composition root") -> "Composition Root"
                    v.contains("api modules can only") -> "API → Non-API"
                    v.contains("use '") -> "Concrete → Concrete"
                    v.contains("testFixtures cannot") -> "TestFixtures → Concrete"
                    v.contains("tests cannot") -> "Test → Concrete"
                    v.contains("must not depend on") -> "Whitelist"
                    else -> "Other"
                }
            }.forEach { (category, items) ->
                appendLine("### $category")
                appendLine()
                appendLine("| Source | Target | Details |")
                appendLine("|--------|--------|---------|")
                items.forEach { v ->
                    val src = Regex("'([^']+)' \\[").find(v)?.groupValues?.get(1) ?: "?"
                    val tgt = Regex("depends on '([^']+)'").find(v)?.groupValues?.get(1)
                        ?: Regex("must not depend on '([^']+)'").find(v)?.groupValues?.get(1) ?: "?"
                    val detail = when {
                        v.contains("use '") -> Regex("use '([^']+)'").find(v)?.let { "→ ${it.groupValues[1]}" } ?: ""
                        else -> v.substringAfter(" - ", "").take(40)
                    }
                    appendLine("| `$src` | `$tgt` | $detail |")
                }
                appendLine()
            }

            appendLine("---")
            appendLine("See [module-validation.md](docs/development/gradle/module-validation.md) for details.")
        }
    }
}

private data class DependencyRecord(
    val sourceProject: String,
    val targetProject: String,
    val configName: String,
    val isTestFixturesDep: Boolean,
)
