package br.unirio.gedapp.service

import br.unirio.gedapp.repository.TenantRepository
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sql.DataSource
import javax.transaction.Transactional

@Component
class TenantService(
    private val dataSource: DataSource,
    @Autowired private val tenantRepo: TenantRepository
) {

    fun initDatabase(schema: String?): MigrateResult =
        Flyway.configure()
            .locations("db/migration/tenants")
            .dataSource(dataSource)
            .schemas(schema)
            .load()
            .migrate()

    @Transactional
    fun renameSchema(oldSchemaName: String, newSchemaName: String) =
        tenantRepo.renameSchema(oldSchemaName, newSchemaName)

    @Transactional
    fun dropSchema(schemaName: String) =
        tenantRepo.dropSchema(schemaName)
}