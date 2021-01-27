package br.unirio.gedapp.configuration

import br.unirio.gedapp.Application
import org.hibernate.MultiTenancyStrategy
import org.hibernate.cfg.Environment

import java.util.HashMap

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean

import org.hibernate.context.spi.CurrentTenantIdentifierResolver

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider

import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter

import org.springframework.orm.jpa.JpaVendorAdapter
import javax.sql.DataSource

@Configuration
class HibernateConfig {

    @Bean
    fun jpaVendorAdapter(): JpaVendorAdapter = HibernateJpaVendorAdapter()

    @Bean
    fun entityManagerFactory(
        dataSource: DataSource,
        jpaProperties: JpaProperties,
        tenantConnectionProvider: MultiTenantConnectionProvider,
        tenantIdentifierResolver: CurrentTenantIdentifierResolver
    ): LocalContainerEntityManagerFactoryBean {

        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan(Application::class.java.getPackage().name)
        em.jpaVendorAdapter = jpaVendorAdapter()

        val jpaPropertiesMap: MutableMap<String, Any?> = HashMap(jpaProperties.properties)
        jpaPropertiesMap[Environment.MULTI_TENANT] = MultiTenancyStrategy.SCHEMA
        jpaPropertiesMap[Environment.MULTI_TENANT_CONNECTION_PROVIDER] = tenantConnectionProvider
        jpaPropertiesMap[Environment.MULTI_TENANT_IDENTIFIER_RESOLVER] = tenantIdentifierResolver
        em.setJpaPropertyMap(jpaPropertiesMap)

        return em
    }
}