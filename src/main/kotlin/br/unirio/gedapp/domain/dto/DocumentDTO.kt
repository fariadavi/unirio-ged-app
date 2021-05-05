package br.unirio.gedapp.domain.dto

import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DocumentDTO(
    var id: String,
    var fileName: String,
    var title: String,
    var summary: String,
    var date: LocalDate,
    var status: DocumentStatus,
    var categoryId: Long,
    var registeredById: Long,
    var category: String? = null,
    var fullCategoryHierarchy: String? = null,
    var registeredBy: String? = null
) {
    constructor(document: Document) : this(
        document.id!!,
        document.fileName,
        document.title,
        document.summary!!,
        document.date!!,
        document.status,
        document.category,
        document.registeredBy
    )

    val formattedDate: String
        get() = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
