package br.unirio.gedapp.domain.dto

import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DocumentDTO(
    var id: String? = null,
    var fileName: String? = null,
    var title: String,
    var summary: String,
    var mediaType: String? = null,
    var date: LocalDate? = null,
    var status: DocumentStatus? = null,
    var categoryId: Long,
    var registeredById: Long? = null,
    var category: String? = null,
    var fullCategoryHierarchy: String? = null,
    var registeredBy: String? = null
) {
    val formattedDate: String?
        get() = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    constructor(document: Document) : this(
        document.id!!,
        document.fileName,
        document.title,
        document.summary ?: "",
        document.mediaType?: "",
        document.date,
        DocumentStatus.values()[document.status],
        document.category,
        document.registeredBy
    )
}
