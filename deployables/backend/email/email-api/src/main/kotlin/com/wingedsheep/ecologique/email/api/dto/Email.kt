package com.wingedsheep.ecologique.email.api.dto

data class Email(
    val to: EmailAddress,
    val subject: String,
    val body: EmailBody,
) {
    init {
        require(subject.isNotBlank()) { "Subject cannot be blank" }
        require(subject.length <= 255) { "Subject must not exceed 255 characters" }
    }
}

sealed class EmailBody {
    data class Plain(val text: String) : EmailBody() {
        init {
            require(text.isNotBlank()) { "Email body cannot be blank" }
        }
    }

    data class Html(val html: String, val plainTextFallback: String) : EmailBody() {
        init {
            require(html.isNotBlank()) { "HTML body cannot be blank" }
            require(plainTextFallback.isNotBlank()) { "Plain text fallback cannot be blank" }
        }
    }
}
