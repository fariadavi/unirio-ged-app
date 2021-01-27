package br.unirio.gedapp.configuration

import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.repository.DepartmentRepository
import org.flywaydb.core.Flyway

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer
import javax.sql.DataSource

@Configuration
class FlywayConfig {

    @Bean
    fun flyway(dataSource: DataSource?): Flyway {
        val flyway = Flyway.configure()
            .locations("db/migration/public")
            .dataSource(dataSource)
            .schemas(TenantIdentifierResolver.DEFAULT_TENANT)
            .load()
        flyway.migrate()
        return flyway
    }

    @Bean
    fun commandLineRunner(deptRepository: DepartmentRepository, dataSource: DataSource?): CommandLineRunner =
        CommandLineRunner {
            deptRepository.findAll()
                .forEach(
                    Consumer { dept: Department ->
                        val tenant = dept.acronym.toLowerCase()
                        Flyway.configure()
                            .locations("db/migration/tenants")
                            .dataSource(dataSource)
                            .schemas(tenant)
                            .load()
                            .migrate()
                    }
                )
        }
}