# ADR-011: Spring Data JDBC over JPA

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We use **Spring Data JDBC** instead of JPA/Hibernate for persistence.

---

## Rationale

JPA/Hibernate often leads to unintended performance problems due to reduced control over queries:

- **Lazy loading surprises**: N+1 queries happen silently
- **Implicit fetching**: Unclear when database calls occur
- **Session management**: Detached entities, dirty checking complexity
- **Magic behavior**: Hard to predict what SQL executes

Spring Data JDBC provides explicit, predictable database access:
```kotlin
// What you write is what executes
@Repository
internal interface ProductRepositoryJdbc : CrudRepository<ProductEntity, String> {
    fun findByCategoryCode(categoryCode: String): List<ProductEntity>
}
```

---

## Trade-offs

### Positive

- Full control over queries
- No lazy loading issues
- Simpler mental model
- Better fit for DDD aggregates (no entity relationships across aggregates)

### Negative

- No automatic relationship mapping
- Manual joins when needed
- Less "magic" means more explicit code

---

## Guidelines

- Use `CrudRepository` for basic operations
- Write custom queries with `@Query` when needed
- Keep entities simple (no `@OneToMany`, `@ManyToOne`)
- Aggregates are loaded completely or not at all
