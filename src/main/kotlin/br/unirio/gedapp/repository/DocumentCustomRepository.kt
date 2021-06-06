package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.Document
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
        maxDate: LocalDate?
    ): Pair<Long, Iterable<Document>>
}