package com.wingedsheep.ecologique.cucumber

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class TestApiClient(
    private val context: ScenarioContext,
    private val environment: Environment
) {
    private val port: Int
        get() = environment.getProperty("local.server.port", Int::class.java)
            ?: throw IllegalStateException("Server port not available")

    fun get(path: String): Response =
        request().get(path)

    fun post(path: String, body: Any): Response =
        request().body(body).post(path)

    fun put(path: String, body: Any): Response =
        request().body(body).put(path)

    fun delete(path: String): Response =
        request().delete(path)

    private fun request() = RestAssured.given()
        .port(port)
        .basePath("")
        .contentType(ContentType.JSON)
        .apply {
            context.authToken?.let { header("Authorization", "Bearer $it") }
        }
}
