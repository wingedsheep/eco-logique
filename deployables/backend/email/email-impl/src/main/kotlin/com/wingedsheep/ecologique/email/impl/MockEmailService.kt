package com.wingedsheep.ecologique.email.impl

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.email.api.EmailService
import com.wingedsheep.ecologique.email.api.dto.Email
import com.wingedsheep.ecologique.email.api.dto.EmailBody
import com.wingedsheep.ecologique.email.api.error.EmailError
import org.springframework.stereotype.Service
import java.util.logging.Logger

/**
 * Mock implementation of [EmailService] for development and testing.
 *
 * This implementation logs email details instead of actually sending them.
 * It also stores sent emails in memory for test verification.
 */
@Service
internal class MockEmailService : EmailService {

    private val logger = Logger.getLogger(MockEmailService::class.java.name)
    private val sentEmails = mutableListOf<SentEmail>()

    override fun send(email: Email): Result<Unit, EmailError> {
        logger.info(
            """
            |
            |========================================
            | MOCK EMAIL
            |========================================
            | To: ${email.to.value}
            | Subject: ${email.subject}
            | Body: ${email.body.toLogString()}
            |========================================
            """.trimMargin()
        )

        sentEmails.add(
            SentEmail(
                to = email.to.value,
                subject = email.subject,
                body = email.body.toLogString()
            )
        )

        return Result.ok(Unit)
    }

    /**
     * Returns all emails sent through this mock service.
     * Useful for test assertions.
     */
    fun getSentEmails(): List<SentEmail> = sentEmails.toList()

    /**
     * Clears all stored sent emails.
     * Call this in test setup to ensure clean state.
     */
    fun clearSentEmails() {
        sentEmails.clear()
    }

    /**
     * Finds emails sent to a specific address.
     */
    fun findEmailsTo(to: String): List<SentEmail> =
        sentEmails.filter { it.to == to }

    private fun EmailBody.toLogString(): String = when (this) {
        is EmailBody.Plain -> text
        is EmailBody.Html -> plainTextFallback
    }
}

data class SentEmail(
    val to: String,
    val subject: String,
    val body: String,
)
