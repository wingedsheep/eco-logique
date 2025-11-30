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

include("deployables:backend:application")
include("common:common-time")
include("common:common-money")
include("common:common-country")
include("common:common-result")
include("deployables:backend:products:products-api")
include("deployables:backend:products:products-impl")
include("deployables:backend:products:products-worldview")
