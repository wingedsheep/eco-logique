plugins {
    id("economique.kotlin-common")
}

dependencies {
    testImplementation("io.cucumber:cucumber-java")
    testImplementation("io.cucumber:cucumber-junit-platform-engine")
    testImplementation("io.cucumber:cucumber-spring")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
}
