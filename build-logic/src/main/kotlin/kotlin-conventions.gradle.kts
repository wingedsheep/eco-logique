import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinConventions : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.jvm")
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.spring")

        target.tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_21.toString()
            }
        }

        target.dependencies {
            "testImplementation"(kotlin("test"))
            "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        }
    }
}
