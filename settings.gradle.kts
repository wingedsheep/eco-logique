pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "economique-root"

// Common modules (shared across all deployables)
include(
    ":common:common-time",
    ":common:common-country",
    ":common:common-money"
)

// Economique deployable modules
include(
    ":deployables:economique:application",
    ":deployables:economique:payment",
    ":deployables:economique:products",
    ":deployables:economique:shipping",
    ":deployables:economique:inventory",
    ":deployables:economique:users",
    ":deployables:economique:worldview-loader",
    ":deployables:economique:test"
)