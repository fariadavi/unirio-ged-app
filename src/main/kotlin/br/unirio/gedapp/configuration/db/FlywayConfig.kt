package br.unirio.gedapp.configuration.db

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.domain.Permission
import br.unirio.gedapp.domain.converter.PermissionEnumSetTypeDescriptor
import org.flywaydb.core.Flyway

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.sql.DataSource

@Configuration
class FlywayConfig {

    @Bean
    fun flyway(dataSource: DataSource?): Flyway {
        val flyway = Flyway.configure()
            .placeholders(
                mapOf(
                    "application_user_email" to
                            System.getenv("APPLICATION_USER_EMAIL"),
                    "starting_system_permissions" to
                            EnumSet.copyOf(Permission.getSystemPermissions())
                                .joinToString(PermissionEnumSetTypeDescriptor.SEPARATOR)
                )
            )
            .locations("db/migration/public")
            .dataSource(dataSource)
            .schemas(TenantIdentifierResolver.DEFAULT_TENANT)
            .load()
        flyway.migrate()
        return flyway
    }
}