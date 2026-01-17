package com.wingedsheep.ecologique.common.outbox

import com.wingedsheep.ecologique.common.outbox.internal.OutboxEventSerializer
import com.wingedsheep.ecologique.common.outbox.internal.OutboxProcessor
import com.wingedsheep.ecologique.common.outbox.internal.OutboxRepository
import com.wingedsheep.ecologique.common.outbox.internal.OutboxRepositoryImpl
import com.wingedsheep.ecologique.common.outbox.internal.OutboxJdbcRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

/**
 * Auto-configuration for the transactional outbox.
 *
 * Enabled by default, can be disabled by setting `outbox.enabled=false`.
 *
 * Note: The application must include the outbox package in its JDBC repository scanning.
 * Add `com.wingedsheep.ecologique.common.outbox.internal` to your @EnableJdbcRepositories basePackages.
 */
@AutoConfiguration
@ConditionalOnProperty(name = ["outbox.enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OutboxProperties::class)
@EnableScheduling
class OutboxAutoConfiguration {

    @Bean
    fun outboxObjectMapper(): ObjectMapper {
        return JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .findAndAddModules()
            .build()
    }

    @Bean
    fun outboxEventSerializer(outboxObjectMapper: ObjectMapper): OutboxEventSerializer {
        return OutboxEventSerializer(outboxObjectMapper)
    }

    @Bean
    fun outboxRepository(outboxJdbcRepository: OutboxJdbcRepository): OutboxRepository {
        return OutboxRepositoryImpl(outboxJdbcRepository)
    }

    @Bean
    fun outboxEventPublisher(
        outboxRepository: OutboxRepository,
        outboxEventSerializer: OutboxEventSerializer,
        applicationEventPublisher: ApplicationEventPublisher
    ): OutboxEventPublisher {
        return OutboxEventPublisher(outboxRepository, outboxEventSerializer, applicationEventPublisher)
    }

    @Bean
    fun outboxProcessor(
        outboxRepository: OutboxRepository,
        outboxEventSerializer: OutboxEventSerializer,
        applicationEventPublisher: ApplicationEventPublisher,
        outboxProperties: OutboxProperties
    ): OutboxProcessor {
        return OutboxProcessor(outboxRepository, outboxEventSerializer, applicationEventPublisher, outboxProperties)
    }
}
