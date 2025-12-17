# ADR-011: Spring Data JDBC over JPA

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We use **Spring Data JDBC** instead of JPA/Hibernate for persistence.

---

## Rationale

JPA/Hibernate leads to unintended performance problems:
- **Lazy loading surprises**: N+1 queries happen silently
- **Implicit fetching**: Unclear when database calls occur
- **Session management**: Detached entities, dirty checking complexity

Spring Data JDBC provides explicit, predictable database access:
```kotlin
@Repository
internal interface ProductRepositoryJdbc : CrudRepository<ProductEntity, String> {
    fun findByCategoryCode(categoryCode: String): List<ProductEntity>
}
```

---

## Guidelines

- Use `CrudRepository` for basic operations
- Write custom queries with `@Query` when needed
- Keep entities simple (no relationship annotations)
- Aggregates are loaded completely or not at all

---

## Consequences

### Positive
- Full control over queries
- No lazy loading issues
- Simpler mental model
- Better fit for DDD aggregates

### Negative
- No automatic relationship mapping
- Manual joins when needed
