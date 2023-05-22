package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.configuration.yml.StorageConfig
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import br.unirio.gedapp.domain.dto.DocumentDTO
import br.unirio.gedapp.domain.dto.GoogleDriveDocumentDTO
import br.unirio.gedapp.domain.dto.SearchDocumentsResultDTO
import br.unirio.gedapp.repository.DocumentRepository
import br.unirio.gedapp.util.FileUtils
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class DocumentService(
    @Autowired storageConfig: StorageConfig,
    private val catSvc: CategoryService,
    private val userSvc: UserService,
    private val docRepo: DocumentRepository,
    private val tenantResolver: TenantIdentifierResolver
) {
    val fileUtils = FileUtils(storageConfig)

    fun getFile(document: Document) =
        fileUtils.getFile(document.tenant, document.id!!, document.fileName)

    fun getById(id: String) =
        docRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }
            .takeIf { it.tenant == tenantResolver.resolveCurrentTenantIdentifier() }
            ?: throw ResourceNotFoundException()

    fun insert(document: Document, file: MultipartFile? = null): Document {
        var newDoc = document

        if (file != null)
            newDoc = document.copy(fileName = file.originalFilename!!)

        newDoc = docRepo.save(newDoc)

        if (file != null)
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
            val existingDocFile = getFile(existingDoc)

            if (!existingDocFile.readBytes().contentEquals(file.bytes)) {
                existingDoc = existingDoc.copy(status = DocumentStatus.NOT_PROCESSED, content = "", mediaType = null)

                updateDocumentFile(existingDoc, file, existingDocFile)
            }

            existingDoc = existingDoc.copy(fileName = file.originalFilename!!)
        }

        return docRepo.save(existingDoc)
    }

    fun updateDocumentFile(doc: Document, file: MultipartFile, currentFile: File?) = runBlocking {
        // process file content asynchronously
        launch {
            fileUtils.transferFile(file, doc.tenant, doc.id!!)
            if (currentFile != null)
                fileUtils.deleteFile(doc.tenant, currentFile.name)

            processFile(doc.copy(fileName = file.originalFilename!!))

            //TODO somehow notify user that the file status has been updated for either success or fail
        }
    }

    fun importGoogleFiles(docMap: Map<Document, GoogleDriveDocumentDTO>) {
        val googleCredential = GoogleCredential()
        googleCredential.accessToken = docMap.values.first().token

        val service: Drive = Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            googleCredential
        ).build()

        docMap.forEach { doc ->
            val document = doc.key
            val driveDoc = doc.value

            try {
                val filepath = fileUtils.getFilePath(document.tenant, document.id!!, document.fileName)
                val outputStream = FileOutputStream(filepath.toString())

                val driveFiles = service.files()
                when (driveDoc.type) {
                    "file" -> driveFiles.get(driveDoc.id).executeMediaAndDownloadTo(outputStream)
                    "document" -> driveFiles.export(driveDoc.id, "application/pdf").executeMediaAndDownloadTo(outputStream)
                    else -> return
                }
            } catch (e: GoogleJsonResponseException) {
                System.err.println("Unable to download file: " + e.details)
                fileUtils.deleteFile(document.tenant, document.id!!, document.fileName)
                throw e
            }

            processFile(document)

            //TODO somehow notify user that the file status has been updated for either success or fail
        }
    }

    private fun processFile(doc: Document) {
        val filepath = fileUtils.getFilePath(doc.tenant, doc.id!!, doc.fileName)
        var docCopy = docRepo.save(doc.copy(status = DocumentStatus.PROCESSING))

        val (docContent, mediaType, extractionStatus) = extractContents(filepath)
        docCopy = docCopy.copy(content = docContent, mediaType = mediaType, status = extractionStatus)

        if (extractionStatus === DocumentStatus.FAILED)
            docCopy = doc.copy(content = "", mediaType = null)

        docRepo.save(docCopy)
    }

    private fun extractContents(filepath: Path): Triple<String, String, DocumentStatus> {
        var docContent = ""
        var mediaType = ""
        var extractionStatus = DocumentStatus.PROCESSED

        try {
            docContent = Tika().parseToString(filepath)
            mediaType = Tika().detect(filepath)
            if (docContent.isBlank()) extractionStatus = DocumentStatus.EMPTY_CONTENT
        } catch (e: Exception) {
            extractionStatus = DocumentStatus.FAILED
            e.printStackTrace() // TODO log properly this error
        }

        return Triple(docContent, mediaType, extractionStatus)
    }

    fun deleteById(id: String) {
        val existingDoc = getById(id)
        docRepo.deleteById(id)

        fileUtils.deleteFile(existingDoc.tenant, id, existingDoc.fileName)
    }

    fun queryDocuments(
        queryString: String,
        page: Int,
        pageSize: Int,
        minDate: LocalDate?,
        maxDate: LocalDate?,
        categoryId: Long?,
        onlyMyDocs: Boolean
    ): SearchDocumentsResultDTO {
        if (categoryId != null && !catSvc.existsById(categoryId))
            throw ResourceNotFoundException()

        val userId = if (onlyMyDocs) userSvc.getCurrentUser().id else null

        val (totalHits, docs) = docRepo.queryDocuments(
            tenantResolver.resolveCurrentTenantIdentifier(),
            queryString,
            page,
            pageSize,
            categoryId,
            userId,
            minDate,
            maxDate
        )

        return SearchDocumentsResultDTO(page, pageSize, totalHits, docs.map { createDTO(it) })
    }

    fun createDTO(document: Document): DocumentDTO {
        val documentDTO = DocumentDTO(document)

        val category = catSvc.getById(document.category)
        documentDTO.category = category.name
        documentDTO.fullCategoryHierarchy = catSvc.getCategoryAncestorsFlattened(category)

        documentDTO.registeredBy = userSvc.getById(document.registeredBy).fullName

        return documentDTO
    }

    fun importGoogleDocs(gDocuments: List<GoogleDriveDocumentDTO>) = runBlocking {
        val currentTenant = tenantResolver.resolveCurrentTenantIdentifier()
        val userId = userSvc.getCurrentUser().id

        launch {
            val docsMap = gDocuments
                .associateBy { saveGoogleDoc(it, userId, currentTenant) }
                .mapNotNull { (k, v) -> k?.let { it to v } }
                .toMap()

            val users = gDocuments.map { it.email }.toSet()
            for (user in users)
                importGoogleFiles(docsMap.filter { it.value.email == user })
        }

        println("teste de async")
    }

    private fun saveGoogleDoc(
        googleDoc: GoogleDriveDocumentDTO,
        userId: Long,
        tenant: String
    ): Document? =
        try {
            insert(
                Document(
                    tenant = tenant,
                    fileName = googleDoc.name,
                    title = googleDoc.name,
                    summary = googleDoc.description,
                    mediaType = googleDoc.mimeType,
                    category = googleDoc.category,
                    date = LocalDate.parse(googleDoc.date),
                    registeredAt = LocalDateTime.now(),
                    registeredBy = userId
                )
            )
        } catch (e: Exception) {
            System.err.println("Unable to save document")
            e.printStackTrace()
            null
        }
}