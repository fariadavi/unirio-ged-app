package br.unirio.gedapp.repository

import org.springframework.stereotype.Component
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Component
class TenantRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun renameSchema(oldSchemaName: String, newSchemaName: String) {
        entityManager
            .createNativeQuery("ALTER SCHEMA $oldSchemaName RENAME TO $newSchemaName")
            .executeUpdate()
    }

    fun dropSchema(schemaName: String) {
        entityManager
            .createNativeQuery("DROP SCHEMA $schemaName CASCADE")
            .executeUpdate()
    }
}