pluginManagement {
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ecologique"

include("deployables:ecologique")
include("deployables:ecologique:application")
