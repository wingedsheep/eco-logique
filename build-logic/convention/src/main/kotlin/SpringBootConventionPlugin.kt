import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

class SpringBootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("economique.kotlin-common")
                apply("org.jetbrains.kotlin.plugin.spring")
                apply("org.springframework.boot")
                apply("io.spring.dependency-management")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", platform(SpringBootPlugin.BOM_COORDINATES))
                add("implementation", libs.findBundle("jackson").get())

                add("testImplementation", libs.findBundle("testing").get())
                add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
            }

            // Disable bootJar by default (only enable for application module)
            tasks.named("bootJar", BootJar::class.java) {
                enabled = false
            }

            extensions.configure<JavaPluginExtension> {
                withSourcesJar()
            }
        }
    }
}