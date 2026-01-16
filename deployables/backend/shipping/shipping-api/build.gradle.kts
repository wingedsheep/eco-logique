plugins {
    id("common-library")
}

dependencies {
    api(project(":common:common-result"))
    api(project(":common:common-country"))
    api(project(":orders:orders-api"))
    api(project(":inventory:inventory-api"))
}
