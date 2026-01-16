package com.wingedsheep.ecologique.email.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.email.api.dto.Email
import com.wingedsheep.ecologique.email.api.error.EmailError

/**
 * Service for sending emails.
 *
 * This interface defines the contract for email delivery. Implementations may
 * send emails through various providers (SMTP, SendGrid, AWS SES, etc.) or
 * use a mock implementation for testing and development.
 */
interface EmailService {

    /**
     * Sends an email.
     *
     * @param email The email to send, including recipient, subject, and body
     * @return [Result.Ok] with [Unit] if the email was accepted for delivery,
     *         or [Result.Err] with an [EmailError] if sending failed
     */
    fun send(email: Email): Result<Unit, EmailError>
}
