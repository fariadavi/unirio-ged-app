package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.Document
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface DocumentRepository : ElasticsearchRepository<Document, String> {

    fun countByTenant(tenant: String): Int

    fun findAllByTenant(tenant: String): List<Document>
}