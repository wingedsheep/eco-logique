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

include(
    ":common:common-time",
    ":common:common-country",
    ":common:common-money",

    ":deployables:backend:application",

    ":deployables:backend:payment:payment-api",
    ":deployables:backend:payment:payment-impl",
    ":deployables:backend:payment:payment-worldview",

    ":deployables:backend:products:products-api",
    ":deployables:backend:products:products-impl",
    ":deployables:backend:products:products-worldview",

    ":deployables:backend:shipping:shipping-api",
    ":deployables:backend:shipping:shipping-impl",
    ":deployables:backend:shipping:shipping-worldview",

    ":deployables:backend:inventory:inventory-api",
    ":deployables:backend:inventory:inventory-impl",
    ":deployables:backend:inventory:inventory-worldview",

    ":deployables:backend:users:users-api",
    ":deployables:backend:users:users-impl",
    ":deployables:backend:users:users-worldview",

    ":deployables:backend:test"
)
