package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import org.springframework.data.repository.NoRepositoryBean
import java.time.LocalDate

@NoRepositoryBean
interface DocumentCustomRepository {

    fun queryDocuments(
        tenant: String,
        text: String,
        page: Int,
        pageSize: Int,
        category: Long?,
        user: Long?,
        minDate: LocalDate?,
        maxDate: LocalDate?,
        status: DocumentStatus?
    ): Pair<Long, Iterable<Document>>

    fun getMapOfCategoriesWithDocCount(tenant: String) : Map<Long, Long>

    fun getDocCountByCategory(tenant: String, categoryId: Long) : Long

    fun updateDocsTenantAcronym(tenant: String, newTenant: String) : Long
}