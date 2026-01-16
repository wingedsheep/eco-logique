package com.wingedsheep.ecologique.users.impl.cucumber

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.email.api.EmailService
import com.wingedsheep.ecologique.email.api.dto.Email
import com.wingedsheep.ecologique.email.api.error.EmailError
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.util.logging.Logger

@TestConfiguration
class TestEmailConfig {

    @Bean
    fun emailService(): EmailService = MockEmailService()
}

class MockEmailService : EmailService {
    private val logger = Logger.getLogger(MockEmailService::class.java.name)
    private val sentEmails = mutableListOf<Email>()

    override fun send(email: Email): Result<Unit, EmailError> {
        logger.info("MOCK EMAIL SERVICE: Sending email to ${email.to.value}")
        sentEmails.add(email)
        return Result.ok(Unit)
    }

    fun getSentEmails(): List<Email> = sentEmails.toList()

    fun clearSentEmails() {
        sentEmails.clear()
    }
}
