package com.wingedsheep.ecologique.users.impl.infrastructure.persistence

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.users.impl.domain.Address
import com.wingedsheep.ecologique.users.impl.domain.Email
import com.wingedsheep.ecologique.users.impl.domain.User
import com.wingedsheep.ecologique.users.impl.domain.UserId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = ["com.wingedsheep.ecologique.users.impl.infrastructure.persistence"])
class UserRepositoryImplIntegrationTest {

    companion object {
        @Container
        @JvmField
        val postgresContainer = PostgreSQLContainer("postgres:14-alpine").apply {
            withDatabaseName("ecologique")
            withUsername("user")
            withPassword("password")
        }

        @JvmStatic
        @DynamicPropertySource
        fun setDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.flyway.locations") { "classpath:db/migration/users" }
            registry.add("spring.flyway.create-schemas") { "true" }
        }
    }

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    @Autowired
    private lateinit var jdbcRepository: UserRepositoryJdbc

    @BeforeEach
    fun cleanup() {
        jdbcRepository.deleteAll()
    }

    @Test
    fun `save should persist and return user`() {
        // Given
        val user = buildUser(name = "Integration Test User")

        // When
        val saved = userRepository.save(user)

        // Then
        assertThat(saved.id).isEqualTo(user.id)
        assertThat(saved.name).isEqualTo("Integration Test User")
    }

    @Test
    fun `save should update existing user`() {
        // Given
        val user = buildUser(name = "Original Name")
        userRepository.save(user)

        val updatedUser = user.copy(name = "Updated Name")

        // When
        val saved = userRepository.save(updatedUser)

        // Then
        assertThat(saved.name).isEqualTo("Updated Name")
        assertThat(userRepository.findById(user.id)?.name).isEqualTo("Updated Name")
    }

    @Test
    fun `findById should return user when exists`() {
        // Given
        val user = buildUser()
        userRepository.save(user)

        // When
        val found = userRepository.findById(user.id)

        // Then
        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(user.id)
    }

    @Test
    fun `findById should return null when not exists`() {
        // Given & When
        val found = userRepository.findById(UserId(UUID.fromString("00000000-0000-0000-0000-000000000000")))

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `findByExternalSubject should return user when exists`() {
        // Given
        val user = buildUser(externalSubject = "auth0|unique123")
        userRepository.save(user)

        // When
        val found = userRepository.findByExternalSubject("auth0|unique123")

        // Then
        assertThat(found).isNotNull
        assertThat(found?.externalSubject).isEqualTo("auth0|unique123")
    }

    @Test
    fun `findByExternalSubject should return null when not exists`() {
        // Given & When
        val found = userRepository.findByExternalSubject("non-existent-subject")

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `findByEmail should return user when exists`() {
        // Given
        val user = buildUser(email = "findbyemail@example.com")
        userRepository.save(user)

        // When
        val found = userRepository.findByEmail(Email("findbyemail@example.com"))

        // Then
        assertThat(found).isNotNull
        assertThat(found?.email?.value).isEqualTo("findbyemail@example.com")
    }

    @Test
    fun `findByEmail should return null when not exists`() {
        // Given & When
        val found = userRepository.findByEmail(Email("nonexistent@example.com"))

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `existsByExternalSubject should return true when exists`() {
        // Given
        val user = buildUser(externalSubject = "auth0|exists")
        userRepository.save(user)

        // When
        val exists = userRepository.existsByExternalSubject("auth0|exists")

        // Then
        assertThat(exists).isTrue()
    }

    @Test
    fun `existsByExternalSubject should return false when not exists`() {
        // Given & When
        val exists = userRepository.existsByExternalSubject("auth0|does-not-exist")

        // Then
        assertThat(exists).isFalse()
    }

    @Test
    fun `existsByEmail should return true when exists`() {
        // Given
        val user = buildUser(email = "exists@example.com")
        userRepository.save(user)

        // When
        val exists = userRepository.existsByEmail(Email("exists@example.com"))

        // Then
        assertThat(exists).isTrue()
    }

    @Test
    fun `existsByEmail should return false when not exists`() {
        // Given & When
        val exists = userRepository.existsByEmail(Email("does-not-exist@example.com"))

        // Then
        assertThat(exists).isFalse()
    }

    @Test
    fun `should persist user with address`() {
        // Given
        val address = Address(
            street = "Main Street",
            houseNumber = "123",
            postalCode = "12345",
            city = "Amsterdam",
            country = Country.NL
        )
        val user = buildUser(address = address)

        // When
        userRepository.save(user)
        val found = userRepository.findById(user.id)

        // Then
        assertThat(found?.defaultAddress).isNotNull
        assertThat(found?.defaultAddress?.street).isEqualTo("Main Street")
        assertThat(found?.defaultAddress?.city).isEqualTo("Amsterdam")
        assertThat(found?.defaultAddress?.country).isEqualTo(Country.NL)
    }

    @Test
    fun `should persist user without address`() {
        // Given
        val user = buildUser(address = null)

        // When
        userRepository.save(user)
        val found = userRepository.findById(user.id)

        // Then
        assertThat(found?.defaultAddress).isNull()
    }

    private fun buildUser(
        id: UserId = UserId.generate(),
        externalSubject: String = "auth0|${System.nanoTime()}",
        name: String = "Test User",
        email: String = "test-${System.nanoTime()}@example.com",
        address: Address? = null
    ): User = User(
        id = id,
        externalSubject = externalSubject,
        name = name,
        email = Email(email),
        defaultAddress = address
    )
}
