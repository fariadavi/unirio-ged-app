package br.unirio.gedapp.domain.dto

data class SearchDocumentsResultDTO(
    val page: Int,
    val pageSize: Int,
    val totalHits: Long,
    val results: List<DocumentDTO>
)