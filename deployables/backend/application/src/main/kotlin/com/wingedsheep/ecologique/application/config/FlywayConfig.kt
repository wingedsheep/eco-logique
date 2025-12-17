package com.wingedsheep.ecologique.application.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import javax.sql.DataSource

@Configuration
class FlywayConfig(dataSource: DataSource) {

    private val logger = LoggerFactory.getLogger(FlywayConfig::class.java)
    private val migrationModulePattern = Regex(".*/db/migration/([^/]+)/.*")

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

    private fun discoverMigrationModules(): List<String> {
        val resolver = PathMatchingResourcePatternResolver()
        return resolver.getResources("classpath*:db/migration/*/*.sql")
            .mapNotNull { resource ->
                runCatching { resource.uri.toString() }
                    .mapCatching { uri -> migrationModulePattern.find(uri)?.groupValues?.get(1) }
                    .getOrNull()
            }
            .distinct()
            .also { logger.info("Discovered Flyway migration modules: {}", it) }
    }
}
