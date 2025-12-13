# ADR-012: Validation in Init Blocks

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We validate domain objects in Kotlin `init` blocks instead of using JSR-303 annotations (`@NotNull`, `@Size`, etc.).

---

## Rationale

JSR-303 validation requires `@Valid` or `@Validated` annotations at call sites to trigger validation. These are easy to forget, leading to invalid objects passing through silently.
```kotlin
// ✗ Bad - Validation only runs if caller remembers @Valid
data class Product(
    @field:NotBlank val name: String,
    @field:Positive val price: BigDecimal
)

fun createProduct(@Valid request: ProductRequest) // Easy to forget @Valid
```

Init block validation is **always enforced** at construction time:
```kotlin
// ✓ Good - Impossible to create invalid Product
data class Product(
    val name: String,
    val price: BigDecimal
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(price > BigDecimal.ZERO) { "Price must be positive" }
    }
}
```

---

## Guidelines

- Use `require` for preconditions (argument validation)
- Use `check` for state invariants
- Throw `IllegalArgumentException` with clear messages
- Validate at domain boundaries (value objects, entities)

---

## Consequences

### Positive

- Validation cannot be bypassed
- No annotation processing required
- Clear, readable validation logic
- Works everywhere (not just Spring contexts)

### Negative

- More verbose than annotations
- Validation logic spread across domain classes
