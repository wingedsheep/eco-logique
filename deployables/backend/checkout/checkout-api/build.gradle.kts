plugins {
    id("common-library")
}

dependencies {
    api(project(":common:common-result"))
    api(project(":common:common-money"))
    api(project(":orders:orders-api"))
    api(project(":payment:payment-api"))
}
