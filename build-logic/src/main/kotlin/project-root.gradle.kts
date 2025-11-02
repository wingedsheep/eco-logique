plugins {
    id("org.sonarqube")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "ecologique")
        property("sonar.organization", "ecologique")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
