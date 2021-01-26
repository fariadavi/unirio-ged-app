package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.Document
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface DocumentRepository : ElasticsearchRepository<Document, String> {}