# ADR-003: Separate Database Schemas per Bounded Context

**Status**: Accepted

**Date**: 2024-11-02

---

## Decision

Each module owns its data exclusively. We support two approaches based on team needs:

1. **Table prefixes** (simpler): One schema, tables prefixed by module name
2. **Separate schemas** (stronger isolation): One PostgreSQL schema per module

No shared tables, no foreign keys across module boundaries.

---

## Option 1: Table Prefixes (Simpler)

One database, one schema, tables prefixed by module:

```
products_product
products_category
shipping_shipment
shipping_carrier
orders_order
orders_order_line
```

Logical separation without configuration overhead. Cross-module queries are still possible but obvious in code review.

**Use when:**
- Small team with good discipline
- Service extraction isn't planned
- Minimizing infrastructure complexity
- Boundaries might still shift

---

## Option 2: Separate Schemas (Stronger Isolation)

Each module gets its own PostgreSQL schema:

```sql
CREATE SCHEMA products;
CREATE SCHEMA orders;
CREATE SCHEMA shipping;
CREATE SCHEMA inventory;
CREATE SCHEMA payments;
CREATE SCHEMA users;
```

**Use when:**
- Service extraction is planned
- Stronger enforcement needed
- Multiple teams working independently

### Entity Configuration

```kotlin
// products-impl/persistence/ProductEntity.kt
@Table("products", schema = "products")
internal data class ProductEntity(
    @Id val id: String,
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String
)

// shipping-impl/persistence/ShipmentEntity.kt
@Table("shipments", schema = "shipping")
internal data class ShipmentEntity(
    @Id val id: String,
    val orderId: String,
    val productId: String,  // Just an ID, not a foreign key
    val weightGrams: Int,
)
```

### Flyway Configuration

Each schema gets its own Flyway instance with separate migration history:

```kotlin
@Configuration
class FlywayConfig(dataSource: DataSource) {
    init {
        discoverMigrationModules().forEach { moduleName ->
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/$moduleName")
                .schemas(moduleName)
                .defaultSchema(moduleName)
                .createSchemas(true)
                .load()
                .migrate()
        }
    }
}
```

---

## No Cross-Schema Foreign Keys

**Never use foreign keys across module boundaries.**

```sql
-- Don't do this
CREATE TABLE shipping.shipments (
                                    product_id VARCHAR(255) REFERENCES products.products(id)  -- No!
);

-- Do this instead
CREATE TABLE shipping.shipments (
                                    product_id VARCHAR(255) NOT NULL  -- Just data, no constraint
);
```

Validate through the API instead:

```kotlin
@Service
internal class ShipmentServiceImpl(
    private val shipmentRepository: ShipmentRepository,
    private val productService: ProductServiceApi,
) : ShipmentServiceApi {

    override fun createShipment(request: CreateShipmentRequest): Result<ShipmentDto, ShipmentError> {
        // Validate product exists through API
        val product = productService.getProduct(request.productId)
            .getOrElse { return Err(ShipmentError.ProductNotFound(request.productId)) }

        val shipment = Shipment(
            id = ShipmentId.generate(),
            productId = ProductId(request.productId),
            weightGrams = product.weightGrams,  // Copy data we need
        )

        return Ok(shipmentRepository.save(shipment).toDto())
    }
}
```

---

## Copying Data Across Boundaries

Store snapshots of data at transaction time:

```kotlin
internal data class Shipment(
    val id: ShipmentId,
    val productId: ProductId,
    val weightGrams: Int,  // Copied from Products at creation time
)
```

This isn't duplication—it's a snapshot. The weight at time of shipment shouldn't change if the product weight is later updated.

---

## Consequences

### Positive
- Clear data ownership
- Modules can evolve independently
- Easy to extract to separate databases later
- Forces proper API boundaries

### Negative
- No database-enforced referential integrity across modules
- More application code for validation
- Potential for orphaned references (handle with soft deletes or grace periods)
