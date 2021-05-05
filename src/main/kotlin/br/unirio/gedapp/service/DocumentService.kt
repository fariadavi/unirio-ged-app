package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import br.unirio.gedapp.repository.DocumentRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class DocumentService(
    private val docRepo: DocumentRepository,
    private val tenantResolver: TenantIdentifierResolver
) {

    fun getById(id: String): Document =
        docRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }
            .takeIf { it.tenant == tenantResolver.resolveCurrentTenantIdentifier() }
            ?: throw ResourceNotFoundException()

    fun insert(document: Document, file: MultipartFile): Document =
        docRepo.save(document.copy(fileName = file.originalFilename!!))

    fun update(docId: String, newDataDoc: Document, file: MultipartFile?): Document {
        var existingDoc = getById(docId)

        if (newDataDoc.fileName.isNotBlank())
            existingDoc = existingDoc.copy(fileName = newDataDoc.fileName)

        if (newDataDoc.title.isNotBlank())
            existingDoc = existingDoc.copy(title = newDataDoc.title)

        if (newDataDoc.summary.isNotBlank())
            existingDoc = existingDoc.copy(summary = newDataDoc.summary)

        if (newDataDoc.date != null)
            existingDoc = existingDoc.copy(date = newDataDoc.date)

        if (newDataDoc.content.isNotBlank())
            existingDoc = existingDoc.copy(content = newDataDoc.content)

        if (newDataDoc.category != -1L)
            existingDoc = existingDoc.copy(category = newDataDoc.category)

        return docRepo.save(existingDoc)
    }

    fun deleteById(id: String) {
        getById(id)
        docRepo.deleteById(id)
    }

    fun queryDocuments(queryString: String): Iterable<Document> =
        docRepo.findByTenantAndContentMatches(
            tenantResolver.resolveCurrentTenantIdentifier(),
            queryString
        )
}