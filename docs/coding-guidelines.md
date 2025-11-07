# Kotlin Coding Guidelines (Concise)

## 1) General

### Clean Code
- Write self-documenting code with clear variable and method names
- No inline comments - if code needs a comment, refactor it to be clearer
- Apply SOLID principles
- Keep things simple - don't do more than required
- Prefer intention-revealing names over comments.
- Short, single-purpose functions/classes.
- Replace generic values with value types or data classes

### SOLID Principles
- **Single Responsibility**: Each class has one reason to change
- **Open/Closed**: Open for extension, closed for modification
- **Liskov Substitution**: Subtypes must be substitutable for their base types
- **Interface Segregation**: Many specific interfaces over one general interface
- **Dependency Inversion**: Depend on abstractions, not concretions


```kotlin
fun percentOf(total: Int, part: Int): BigDecimal =
    (part.toBigDecimal() / total.toBigDecimal()).setScale(2, RoundingMode.HALF_UP)
```

---

## 2) Naming

* **Taxonomic**: generic → specific (`ProductPriceCalculator`, not `PriceProductCalculator`).
* Booleans: `is/has/can`.
* **Requests/DTOs**: avoid generic names like `body`. Use specific names by intent: `UpdatePriceRequest`,
  `CreateUserRequest`, `CheckoutCommandDto`.
* Tests read like sentences (backticks).

```kotlin
@Test
fun `calculatePrice should apply eco discount`() {
}
```

---

## 3) Data classes & Value types

* `data class` for domain data.
* `@JvmInline value class` for IDs/single-field wrappers.
* Validate invariants in `init`.

```kotlin
data class Money(val amount: BigDecimal, val currency: Currency) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
    }
}
@JvmInline
value class ProductId(val value: String) { init {
    require(value.isNotBlank())
}
}
```

---

## 4) Nullability

* Prefer Kotlin nullable types over `Optional`.
* Never use `!!`. Use safe calls, `?:`, early returns.

```kotlin
val product = repo.findById(id) ?: return Result.failure(NoSuchElementException("Not found"))
```

---

## 5) Functions

* Use expression bodies for single expressions.
* Use **named args** when it clarifies intent.
* Prefer default parameters over overloads.

```kotlin
fun format(m: Money): String = "${m.amount.setScale(2)} ${m.currency}"
val p = buildProduct(name = "Bamboo Toothbrush", weightGrams = 80)
```

---

## 6) Collections & immutability

* Prefer `val` and immutable collections.
* Return empty collections, not `null`.

```kotlin
fun findAll(): List<Product> = storage.toList()
```

---

## 7) Errors & results

* `require`/`check` for programmer errors/invariants.
* Domain use-cases/services may **throw typed domain errors** for business failures.
* Use `Result<T>` only when callers must branch on success/failure without exceptions.

```kotlin
fun reserve(quantity: Int): Result<Unit> = runCatching {
    require(quantity > 0) { "Quantity must be positive" }
    // …
}
```

---

## 8) Extension functions (mappers/utilities)

* Top-level extension functions over utility classes.
* Keep mappers next to DTO/entity definitions.

```kotlin
fun ProductCreateRequest.toDomain() = Product(
    id = ProductId.generate(), name = name, price = Money(priceAmount, priceCurrency), weight = Weight(weightGrams)
)
```

---

## 9) Repositories & Services (style)

* Repositories return **domain types** (not entities).
* Naming contract:

    * `find<X>` → **nullable** (may be absent).
    * `get<X>` → **non-null**, **throws** a typed domain error when missing.
* Concrete, predictable method names.

```kotlin
interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
}

class ProductService(private val repo: ProductRepository) {
    fun findProduct(id: ProductId): Product? =
        repo.findById(id)
    
    fun getProduct(id: ProductId): Product =
        repo.findById(id) ?: throw ProductNotFound(id)
}
```

---

## 10) Logging

* Log at the edges (controllers/adapters), not pure domain.
* Structured messages; never log secrets.

```kotlin
logger.info("Processed product {}", product.id.value)
```

---

## 11) Tests (style & rules)

* JUnit 5 + AssertJ + Mockito-Kotlin.
* **Given/When/Then** comments separate phases (you may combine “Given & When” / “When & Then”). **Given** goes above
  variable declarations.
* Always stub with **concrete arguments** (no loose `any()` in setup).
* No `lenient`. No unnecessary stubbing.
* Prefer `@Mock` / `@InjectMocks`.
* Place **non-test helpers below the tests**.
* Write clean code. Apply SOLID. Keep it simple. Don’t remove existing inline comments; prefer names that make them
  unnecessary.

```kotlin
@ExtendWith(MockitoExtension::class)
class ProductServiceTest {

    @Mock
    lateinit var repo: ProductRepository
    @InjectMocks
    lateinit var service: ProductService

    @Test
    fun `getProduct returns product when exists`() {
        // Given
        val id = ProductId("PROD-001")
        val existing = buildProduct(id = id)
        whenever(repo.findById(id)).thenReturn(existing) // concrete argument

        // When
        val result = service.getProduct(id)

        // Then
        assertThat(result).isEqualTo(existing)
    }

    @Test
    fun `getProduct throws when missing`() {
        // Given
        val id = ProductId("PROD-404")
        whenever(repo.findById(id)).thenReturn(null)

        // When & Then
        assertThatThrownBy { service.getProduct(id) }
            .isInstanceOf(ProductNotFound::class.java)
    }

    // Helpers (below tests)
    private fun buildProduct(id: ProductId) =
        Product(id, "T-Shirt", Money(BigDecimal("29.99"), EUR), Weight(150))
}
```

---

## 12) JSON & Jackson

* Use `jackson-module-kotlin`.
* DTOs mirror the HTTP contract; map at edges.

```kotlin
data class ProductResponseV1(val id: String, val name: String, val priceAmount: BigDecimal, val priceCurrency: Currency)
```

---

## 13) Formatting & toolchain

* Kotlin 2.1, JVM toolchain 21 (configured).
* Default code style; organize imports; no wildcard imports.
* Avoid trailing spaces; suppress unused params with `_` when needed.

```kotlin
fun onEvent(@Suppress("UNUSED_PARAMETER") ignored: Any) { /* … */
}
```

---

## 14) Small patterns we like

* `fold` for `Result` based flows.
* Tiny helpers at the edge to keep controllers clean.

```kotlin
fun <T> Result<T>.orBadRequest(): ResponseEntity<T> =
    fold(onSuccess = { ResponseEntity.ok(it) }, onFailure = { ResponseEntity.badRequest().build() })
```

---

## 15) REST errors with **Zalando Problem** (first-principles, concise)

**Goals:** predictable RFC-7807 payloads; controllers without business decisions; no HTTP concerns in domain code.

**Rules**

1. Keep HTTP out of services/use-cases. They return objects or **throw domain errors**.
2. One `@RestControllerAdvice` maps domain errors → Problem JSON.
3. Use stable `type` URIs; put machine-readable properties (e.g., `productId`), keep `detail` human-readable.
4. Consistent statuses (404/409/422/…); unexpected errors become 500 Problems.

### Dependency (application module)

```kotlin
dependencies { implementation("org.zalando:problem-spring-web:0.29.1") }
```

### Domain errors (explicit, typed)

```kotlin
sealed class DomainError(msg: String) : RuntimeException(msg)
class ProductNotFound(val id: ProductId) : DomainError("Product not found: ${id.value}")
class PriceConflict(val id: ProductId) : DomainError("Price conflict: ${id.value}")
class InvalidRequest(message: String) : DomainError(message)
```

### Use-case (multi-step, returns the object, throws on failure)

```kotlin
fun interface UpdateProductPrice {
    fun invoke(cmd: Command): Product                           // throws DomainError on failure
    data class Command(val id: ProductId, val newPrice: Money)
}

@Service
class UpdateProductPriceImpl(private val repo: ProductRepository) : UpdateProductPrice {
    override fun invoke(cmd: Command): Product {
        require(cmd.newPrice.amount > BigDecimal.ZERO) { "Price must be positive" }
        val current = repo.findById(cmd.id) ?: throw ProductNotFound(cmd.id)
        // optional: detect business conflict and throw PriceConflict(cmd.id)
        return repo.save(current.withUpdatedPrice(cmd.newPrice))
    }
}
```

### Controller (clear names; no generic `body`; no HTTP decisions)

```kotlin
@RestController
@RequestMapping("/api/v1/products")
class ProductController(private val updateProductPrice: UpdateProductPrice) {

    data class UpdatePriceRequest(val amount: BigDecimal, val currency: Currency)

    @PutMapping("/{id}/price")
    fun updatePrice(
        @PathVariable("id") productId: String,
        @RequestBody updatePriceRequest: UpdatePriceRequest
    ): ProductResponseV1 =
        updateProductPrice(
            UpdateProductPrice.Command(
                ProductId(productId),
                Money(updatePriceRequest.amount, updatePriceRequest.currency)
            )
        ).toResponse()
}

private fun Product.toResponse() =
    ProductResponseV1(id.value, name, price.amount, price.currency)
```

### Advice → Problem JSON (single mapping point)

```kotlin
@RestControllerAdvice
class DomainProblemAdvice : ProblemHandling {

    @ExceptionHandler(ProductNotFound::class)
    fun notFound(e: ProductNotFound) = problem(
        "product-not-found", "Product not found",
        Status.NOT_FOUND, e.message
    ).with("productId", e.id.value)

    @ExceptionHandler(PriceConflict::class)
    fun conflict(e: PriceConflict) = problem(
        "conflict", "Conflict",
        Status.CONFLICT, e.message
    ).with("productId", e.id.value)

    @ExceptionHandler(InvalidRequest::class)
    fun unprocessable(e: InvalidRequest) = problem(
        "invalid-request", "Invalid request",
        Status.UNPROCESSABLE_ENTITY, e.message
    )
}

private fun problem(code: String, title: String, status: Status, detail: String?) =
    Problem.builder()
        .withType(URI.create("https://api.economique.com/problems/$code"))
        .withTitle(title)
        .withStatus(status)
        .withDetail(detail)
        .build()

private fun Problem.with(key: String, value: Any) =
    Problem.builder().from(this).with(key, value).build()
```

**Common statuses**

* **400** — bean/JSON validation (Problem defaults and validation advice).
* **401/403** — auth/authorization (Spring Security → Problem).
* **404** — `ProductNotFound`.
* **409** — `PriceConflict`.
* **422** — `InvalidRequest` (domain validation).
* **500** — unexpected (Problem defaults).

### Tests (controller + Problem, follow our rules)

```kotlin
@AutoConfigureMockMvc
@SpringBootTest
class ProductControllerProblemTest(
    @Autowired private val mockMvc: MockMvc,
    @MockBean private val updateProductPrice: UpdateProductPrice
) {
    @Test
    fun `should return 404 Problem when product is missing`() {
        // Given
        val id = "PROD-404"
        val cmd = UpdateProductPrice.Command(ProductId(id), Money(BigDecimal("10.00"), EUR))
        whenever(updateProductPrice.invoke(cmd)).thenAnswer { throw ProductNotFound(ProductId(id)) }

        // When
        val res = mockMvc.perform(
            put("/api/v1/products/$id/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"amount":10.00,"currency":"EUR"}""")
        ).andReturn().response

        // Then
        assertThat(res.status).isEqualTo(404)
        assertThat(res.contentAsString).contains("product-not-found")
    }

    @Test
    fun `should return 422 Problem for invalid price`() {
        // Given & When
        val res = mockMvc.perform(
            put("/api/v1/products/PROD-001/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"amount":-1.00,"currency":"EUR"}""")
        ).andReturn().response

        // Then
        assertThat(res.status).isEqualTo(422)
        assertThat(res.contentAsString).contains("invalid-request")
    }
}
```
