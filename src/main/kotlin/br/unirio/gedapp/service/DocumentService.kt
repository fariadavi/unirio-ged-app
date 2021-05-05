package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import br.unirio.gedapp.repository.DocumentRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import kotlin.concurrent.thread

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

    fun insert(document: Document, file: MultipartFile): Document {
        val newDoc = docRepo.save(document.copy(fileName = file.originalFilename!!))

        updateDocumentFile(newDoc, file, null)

        return newDoc
    }

    fun update(docId: String, newDataDoc: Document, file: MultipartFile?): Document {
        var existingDoc = getById(docId)

        if (newDataDoc.title.isNotBlank())
            existingDoc = existingDoc.copy(title = newDataDoc.title)

        if (newDataDoc.summary.isNotBlank())
            existingDoc = existingDoc.copy(summary = newDataDoc.summary)

        if (newDataDoc.date != null)
            existingDoc = existingDoc.copy(date = newDataDoc.date)

        if (newDataDoc.category != -1L)
            existingDoc = existingDoc.copy(category = newDataDoc.category)

        if (file != null) {
            existingDoc = existingDoc.copy(
                fileName = file.originalFilename!!,
                status = DocumentStatus.NOT_PROCESSED,
                content = ""
            )

            var existingDocFile = File("") //TODO retrieve existing file for current doc
            updateDocumentFile(existingDoc, file, existingDocFile)
        }

        return docRepo.save(existingDoc)
    }

    fun updateDocumentFile(doc: Document, file: MultipartFile, currentFile: File?) {
        // launch a new thread to process file content asynchronously
        thread {
            // TODO move uploaded file to specific folder
            // TODO delete currentFile if exists
            // TODO process file content
        }
    }

    fun deleteById(id: String) {
        getById(id)
        docRepo.deleteById(id)

        //TODO delete existing file for deleted doc
    }

    fun queryDocuments(queryString: String): Iterable<Document> =
        docRepo.findByTenantAndContentMatches(
            tenantResolver.resolveCurrentTenantIdentifier(),
            queryString
        )
}