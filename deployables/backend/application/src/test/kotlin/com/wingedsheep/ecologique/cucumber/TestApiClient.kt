package com.wingedsheep.ecologique.cucumber

import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.Duration

@Component
class TestApiClient(
    private val context: ScenarioContext,
    private val environment: Environment
) {
    private val objectMapper = jacksonObjectMapper()

    private val port: Int
        get() = environment.getProperty("local.server.port", Int::class.java)
            ?: throw IllegalStateException("Server port not available")

    private val client: WebTestClient by lazy {
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .responseTimeout(Duration.ofSeconds(30))
            .build()
    }

    fun get(path: String): TestResponse =
        executeRequest {
            client.get()
                .uri(path)
                .headers { headers -> context.authToken?.let { headers.setBearerAuth(it) } }
                .exchange()
        }

    fun post(path: String, body: Any): TestResponse =
        executeRequest {
            client.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers { headers -> context.authToken?.let { headers.setBearerAuth(it) } }
                .bodyValue(body)
                .exchange()
        }

    fun put(path: String, body: Any): TestResponse =
        executeRequest {
            client.put()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers { headers -> context.authToken?.let { headers.setBearerAuth(it) } }
                .bodyValue(body)
                .exchange()
        }

    fun delete(path: String): TestResponse =
        executeRequest {
            client.delete()
                .uri(path)
                .headers { headers -> context.authToken?.let { headers.setBearerAuth(it) } }
                .exchange()
        }

    private fun executeRequest(block: () -> WebTestClient.ResponseSpec): TestResponse {
        val result = block().returnResult<String>()
        val statusCode = result.status.value()
        val body = result.responseBody.blockFirst() ?: ""
        return TestResponse(statusCode, body, objectMapper)
    }
}

class TestResponse(
    val statusCode: Int,
    private val body: String,
    private val objectMapper: ObjectMapper
) {
    fun getString(path: String): String? {
        val value = getValueAtPath(path) ?: return null
        return when (value) {
            is String -> value
            is Number -> value.toString()
            else -> value.toString()
        }
    }

    fun getInt(path: String): Int? {
        return (getValueAtPath(path) as? Number)?.toInt()
    }

    fun getDouble(path: String): Double? {
        return (getValueAtPath(path) as? Number)?.toDouble()
    }

    fun <T> getList(path: String): List<T> {
        val value = if (path.isEmpty()) {
            parseBody()
        } else {
            getValueAtPath(path)
        }
        @Suppress("UNCHECKED_CAST")
        return value as? List<T> ?: emptyList()
    }

    fun hasKey(key: String): Boolean {
        val parsed = parseBody() as? Map<*, *> ?: return false
        return parsed.containsKey(key)
    }

    fun bodyAsString(): String = body

    private fun getValueAtPath(path: String): Any? {
        if (body.isBlank()) return null
        val parsed = parseBody() ?: return null

        if (path.isEmpty()) return parsed

        var current: Any? = parsed
        for (segment in path.split(".")) {
            current = when (current) {
                is Map<*, *> -> current[segment]
                is List<*> -> current.getOrNull(segment.toIntOrNull() ?: return null)
                else -> return null
            }
        }
        return current
    }

    private fun parseBody(): Any? {
        if (body.isBlank()) return null
        return try {
            objectMapper.readValue<Any>(body)
        } catch (_: Exception) {
            null
        }
    }
}
