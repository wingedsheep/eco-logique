plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.allopen)
    implementation(libs.spring.boot.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kotlinCommon") {
            id = "economique.kotlin-common"
            implementationClass = "KotlinCommonConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "economique.kotlin-library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("springBoot") {
            id = "economique.spring-boot"
            implementationClass = "SpringBootConventionPlugin"
        }
        register("testFixtures") {
            id = "economique.test-fixtures"
            implementationClass = "TestFixturesConventionPlugin"
        }
    }
}