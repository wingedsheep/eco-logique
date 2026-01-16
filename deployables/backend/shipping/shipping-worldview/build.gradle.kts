plugins {
    id("common-library")
}

dependencies {
    api(project(":shipping:shipping-api"))
    api(project(":orders:orders-api"))
    api(project(":inventory:inventory-api"))
}
