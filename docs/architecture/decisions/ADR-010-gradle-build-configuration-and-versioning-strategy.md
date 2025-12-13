# ADR-010: Gradle Build Configuration and Versioning Strategy

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We use **Gradle Version Catalog** for dependency management, **build-logic convention plugins** for shared build configuration, and a **centralized version** in `gradle.properties`.

---

## Version Catalog

All dependency versions are defined in `gradle/libs.versions.toml`:
```toml
[versions]
kotlin = "1.9.22"
spring-boot = "3.2.0"

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
```

Usage in build files:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
}
```

---

## Build Logic

Shared build configuration lives in the `build-logic` module as convention plugins:

- **`kotlin-conventions.gradle.kts`**: Kotlin/Spring projects with test dependencies
- **`common-library.gradle.kts`**: Standalone libraries without Spring

Modules apply these conventions:
```kotlin
plugins {
    id("kotlin-conventions")
}
```

---

## Application Version

The application version is defined once in `gradle.properties`:
```properties
version=0.0.1-SNAPSHOT
```

---

## Consequences

### Positive

- Single source of truth for dependency versions
- Consistent build configuration across modules
- IDE support for version catalog
- Easy upgrades (change version in one place)

### Negative

- Initial setup overhead
- Convention plugins require Kotlin DSL knowledge