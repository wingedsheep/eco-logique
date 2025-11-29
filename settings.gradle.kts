pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ecologique"

include("deployables:backend")
include("common:common-time")
include("common:common-money")
include("common:common-country")
include("common:common-result")
include("products:products-api")
include("products:products-impl")
include("products:products-worldview")
