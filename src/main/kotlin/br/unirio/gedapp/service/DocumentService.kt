package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.configuration.yml.StorageConfig
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import br.unirio.gedapp.repository.DocumentRepository
import br.unirio.gedapp.util.FileUtils
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Path
import kotlin.concurrent.thread

@Service
class DocumentService(
    @Autowired storageConfig: StorageConfig,
    private val docRepo: DocumentRepository,
    private val tenantResolver: TenantIdentifierResolver
) {
    val fileUtils = FileUtils(storageConfig)

    fun getFile(document: Document) =
        fileUtils.getFile(document.tenant, document.id!!, document.fileName)

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

        if (newDataDoc.summary != null)
            existingDoc = existingDoc.copy(summary = newDataDoc.summary)

        if (newDataDoc.date != null)
            existingDoc = existingDoc.copy(date = newDataDoc.date)

        if (newDataDoc.category != -1L)
            existingDoc = existingDoc.copy(category = newDataDoc.category)

        if (file?.originalFilename != null) {
            existingDoc = existingDoc.copy(fileName = file.originalFilename!!)

            val existingDocFile = getFile(existingDoc)
            if (!existingDocFile.readBytes().contentEquals(file.bytes)) {
                existingDoc = existingDoc.copy(status = DocumentStatus.NOT_PROCESSED, content = "")

                updateDocumentFile(existingDoc, file, existingDocFile)
            }
        }

        return docRepo.save(existingDoc)
    }

    fun updateDocumentFile(doc: Document, file: MultipartFile, currentFile: File?) {
        // launch a new thread to process file content asynchronously
        thread {
            fileUtils.transferFile(file, doc.tenant, doc.id!!)
            if (currentFile != null)
                fileUtils.deleteFile(doc.tenant, doc.id!!, doc.fileName)

            processFile(doc)

            //TODO somehow notify user that the file status has been updated for either success or fail
        }
    }

    private fun processFile(doc: Document) {
        val filepath = fileUtils.getFilePath(doc.tenant, doc.id!!, doc.fileName)
        docRepo.save(doc.copy(status = DocumentStatus.PROCESSING))

        var (docContent, extractionStatus) = extractContents(filepath)
        docRepo.save(doc.copy(status = extractionStatus, content = docContent))
    }

    private fun extractContents(filepath: Path): Pair<String, DocumentStatus> {
        var docContent = ""
        var extractionStatus = DocumentStatus.SUCCESS

        try {
            docContent = Tika().parseToString(filepath)
        } catch (e: Exception) {
            extractionStatus = DocumentStatus.FAILED
        }

        return Pair(docContent, extractionStatus)
    }

    fun deleteById(id: String) {
        var existingDoc = getById(id)
        docRepo.deleteById(id)

        fileUtils.deleteFile(existingDoc.tenant, id, existingDoc.fileName)
    }

    fun queryDocuments(queryString: String): Iterable<Document> =
        docRepo.findByTenantAndContentMatches(
            tenantResolver.resolveCurrentTenantIdentifier(),
            queryString
        )
}