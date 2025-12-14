plugins {
    id("common-library")
    `java-test-fixtures`
}

dependencies {
    api(project(":common:common-country"))
    api(project(":common:common-result"))
}
