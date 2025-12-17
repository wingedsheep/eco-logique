# ADR-010: Gradle Build Configuration and Versioning Strategy

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We use **Gradle Version Catalog** for dependency management, **build-logic convention plugins** for shared build configuration, and a **centralized version** in `gradle.properties`.

---

## Version Catalog

All dependency versions in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "1.9.22"
spring-boot = "3.2.0"

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
```

Usage:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
}
```

---

## Build Logic Convention Plugins

Shared configuration in `build-logic/`:

```kotlin
// build-logic/src/main/kotlin/kotlin-conventions.gradle.kts
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    `java-test-fixtures`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

Modules apply with one line:
```kotlin
plugins {
    id("kotlin-conventions")
}
```

---

## Consequences

### Positive
- Single source of truth for versions
- Consistent configuration across modules
- IDE support for version catalog
- Easy upgrades

### Negative
- Initial setup overhead
- Convention plugins require Kotlin DSL knowledge
