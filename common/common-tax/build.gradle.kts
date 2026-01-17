plugins {
    id("common-library")
    `java-test-fixtures`
}

dependencies {
    api(project(":common:common-country"))
    api(project(":products:products-api"))

    testImplementation(libs.assertj.core)
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.3")
}
