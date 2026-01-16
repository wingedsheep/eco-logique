pluginManagement {
    includeBuild("../../build-logic")
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

rootProject.name = "ecologique-backend"

// Common modules
include(":common:common-time")
include(":common:common-money")
include(":common:common-country")
include(":common:common-result")

// Application modules
include(":application")

include(":products:products-api")
include(":products:products-impl")
include(":products:products-worldview")
include(":users:users-api")
include(":users:users-impl")
include(":users:users-worldview")
include(":orders:orders-api")
include(":orders:orders-impl")
include(":orders:orders-worldview")
include(":cart:cart-api")
include(":cart:cart-impl")
include(":cart:cart-worldview")
include(":email:email-api")
include(":email:email-impl")
include(":payment:payment-api")
include(":payment:payment-impl")
include(":payment:payment-worldview")
include(":inventory:inventory-api")
include(":inventory:inventory-impl")
include(":inventory:inventory-worldview")
include(":checkout:checkout-api")
include(":checkout:checkout-impl")
include(":shipping:shipping-api")
include(":shipping:shipping-impl")
include(":shipping:shipping-worldview")

// Map common modules to the actual location
project(":common:common-time").projectDir = file("../../common/common-time")
project(":common:common-money").projectDir = file("../../common/common-money")
project(":common:common-country").projectDir = file("../../common/common-country")
project(":common:common-result").projectDir = file("../../common/common-result")
