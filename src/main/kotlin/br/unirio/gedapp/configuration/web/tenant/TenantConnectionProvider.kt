package br.unirio.gedapp.configuration.web.tenant

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import org.springframework.stereotype.Component
import java.sql.Connection
import javax.sql.DataSource

@Component
class TenantConnectionProvider(private val datasource: DataSource) : MultiTenantConnectionProvider {

    override fun getAnyConnection(): Connection = datasource.connection

    override fun releaseAnyConnection(connection: Connection) = connection.close()

    override fun getConnection(tenantIdentifier: String): Connection {
        val connection: Connection = anyConnection
        connection.createStatement().execute("SET SCHEMA '$tenantIdentifier'")
        return connection
    }

    override fun releaseConnection(tenantIdentifier: String, connection: Connection) {
        val defaultTenant = TenantIdentifierResolver.DEFAULT_TENANT
        connection.createStatement().execute("SET SCHEMA '$defaultTenant'")
        releaseAnyConnection(connection)
    }

    override fun supportsAggressiveRelease(): Boolean = false

    override fun isUnwrappableAs(unwrapType: Class<*>): Boolean = false

    override fun <T> unwrap(unwrapType: Class<T>): T? = null
}