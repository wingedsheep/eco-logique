plugins {
    id("common-library")
    `java-test-fixtures`
}

dependencies {
    api(project(":common:common-money"))
    api(project(":common:common-result"))
    api(project(":products:products-api"))
}
