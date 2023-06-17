package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver.Companion.DEFAULT_TENANT
import br.unirio.gedapp.configuration.yml.StorageConfig
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.controller.exceptions.UnauthorizedException
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
import kotlinx.coroutines.*
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.coroutines.EmptyCoroutineContext

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
        val currentTenant = tenantResolver.resolveCurrentTenantIdentifier()
        if (currentTenant === DEFAULT_TENANT) throw UnauthorizedException()

        var newDoc = document

        if (file != null)
            newDoc = document.copy(fileName = file.originalFilename!!)

        newDoc = docRepo.save(newDoc)

        if (file != null)
            updateDocumentFile(newDoc, file, null)

        return newDoc
    }

    fun update(docId: String, newDataDoc: Document, file: MultipartFile?): Document {
        val currentTenant = tenantResolver.resolveCurrentTenantIdentifier()
        if (currentTenant === DEFAULT_TENANT) throw UnauthorizedException()

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
                existingDoc = existingDoc.copy(status = DocumentStatus.PENDING.ordinal, content = "", mediaType = null)

                updateDocumentFile(existingDoc, file, existingDocFile)
            }

            existingDoc = existingDoc.copy(fileName = file.originalFilename!!)
        }

        return docRepo.save(existingDoc)
    }

    fun updateDocumentFile(doc: Document, file: MultipartFile, currentFile: File?) =
        CoroutineScope(EmptyCoroutineContext).launch { // process file content asynchronously
            fileUtils.transferFile(file, doc.tenant, doc.id!!)
            if (currentFile != null)
                fileUtils.deleteFile(doc.tenant, currentFile.name)

            processFile(doc.copy(fileName = file.originalFilename!!))

            //TODO somehow notify user that the file status has been updated for either success or fail
        }

    private fun processFile(doc: Document) {
        val filepath = fileUtils.getFilePath(doc.tenant, doc.id!!, doc.fileName)
        var docCopy = docRepo.save(doc.copy(status = DocumentStatus.PROCESSING.ordinal))

        val (docContent, mediaType, extractionStatus) = extractContents(filepath)
        docCopy = docCopy.copy(content = docContent, mediaType = mediaType, status = extractionStatus.ordinal)

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
        val currentTenant = tenantResolver.resolveCurrentTenantIdentifier()
        if (currentTenant === DEFAULT_TENANT) throw UnauthorizedException()

        if (categoryId != null && !catSvc.existsById(categoryId))
            throw ResourceNotFoundException()

        val userId = if (onlyMyDocs) userSvc.getCurrentUser().id else null

        val (totalHits, docs) = docRepo.queryDocuments(
            currentTenant,
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

    fun getMapOfCategoriesWithDocCount() =
        tenantResolver.resolveCurrentTenantIdentifier()
            .takeIf { it !== DEFAULT_TENANT }
            ?.let { docRepo.getMapOfCategoriesWithDocCount(it) } ?: emptyMap()

    fun getDocCountByCategory(categoryId: Long) =
        tenantResolver.resolveCurrentTenantIdentifier()
            .takeIf { it !== DEFAULT_TENANT }
            ?.let { docRepo.getDocCountByCategory(it, categoryId) } ?: 0

    fun createDTO(document: Document): DocumentDTO {
        val documentDTO = DocumentDTO(document)

        runCatching {
            catSvc.getById(document.category)
        }.onSuccess {
            documentDTO.category = it.name
            documentDTO.fullCategoryHierarchy = catSvc.getCategoryAncestorsFlattened(it)
        }.onFailure {
            documentDTO.category = "?"
            documentDTO.fullCategoryHierarchy = "?"
        }

        runCatching {
            userSvc.getById(document.registeredBy)
        }.onSuccess {
            documentDTO.registeredBy = it.fullName
        }.onFailure {
            documentDTO.registeredBy = "?"
        }

        return documentDTO
    }

    suspend fun importGoogleDocs(gDocuments: List<GoogleDriveDocumentDTO>) {
        val currentTenant = tenantResolver.resolveCurrentTenantIdentifier()
        if (currentTenant === DEFAULT_TENANT) throw UnauthorizedException()
        val userId = userSvc.getCurrentUser().id

        CoroutineScope(EmptyCoroutineContext).launch {
            val docsMap = coroutineScope {
                return@coroutineScope gDocuments
                    .associateBy { async { saveGoogleDoc(it, userId, currentTenant) }.await() }
                    .mapNotNull { (k, v) -> k?.let { it to v } }
                    .toMap()
            }

            val users = docsMap.values.map { it.email }.toSet()
            for (user in users)
                launch { importGoogleFiles(docsMap.filter { it.value.email == user }) }
        }
    }

    private suspend fun saveGoogleDoc(
        googleDoc: GoogleDriveDocumentDTO,
        userId: Long,
        tenant: String
    ): Document? {
        println("saving " + googleDoc.name + " | " + googleDoc.id) // TODO replace with log
        return try {
            Document(
                tenant = tenant,
                fileName = googleDoc.name.trim(),
                title = googleDoc.name.trim(),
                summary = googleDoc.description,
                mediaType = googleDoc.mimeType,
                category = googleDoc.category,
                date = LocalDate.parse(googleDoc.date),
                registeredAt = LocalDateTime.now(),
                registeredBy = userId
            ).let { insert(it) }
        } catch (e: Exception) {
            System.err.println("Unable to save document " + googleDoc.name + " | " + googleDoc.id) // TODO replace with log
            e.printStackTrace()
            null
        }
    }

    private suspend fun importGoogleFiles(docMap: Map<Document, GoogleDriveDocumentDTO>) = coroutineScope {
        println("importing docs from Google Drive of " + docMap.values.first().email) // TODO replace with log

        val googleCredential = GoogleCredential()
        googleCredential.accessToken = docMap.values.first().token

        val service: Drive = Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            googleCredential
        ).build()

        for (doc in docMap) {
            launch {
                val document = doc.key
                val driveDoc = doc.value

                val outputStream = try {
                    val filepath = fileUtils.getFilePath(document.tenant, document.id!!, document.fileName)
                    FileOutputStream(filepath.toString())
                } catch (e: Exception) {
                    System.err.println("Error creating file named " + document.fileName) // TODO replace with log
                    e.printStackTrace()
                    docRepo.save(document.copy(status = DocumentStatus.FAILED.ordinal))
                    return@launch
                }

                try {
                    val driveFiles = service.files()
                    when (driveDoc.type) {
                        "file" -> driveFiles
                            .get(driveDoc.id)
                            .executeMediaAndDownloadTo(outputStream)

                        "document" -> driveFiles
                            .export(driveDoc.id, "application/pdf")
                            .executeMediaAndDownloadTo(outputStream)

                        else -> return@launch
                    }
                } catch (e: GoogleJsonResponseException) {
                    System.err.println("Unable to download file from Google Drive: " + e.details) // TODO replace with log
                    fileUtils.deleteFile(document.tenant, document.id, document.fileName)
                    docRepo.save(document.copy(status = DocumentStatus.FAILED.ordinal))
                    return@launch
                } catch (e: Exception) {
                    System.err.println("Error importing file " + document.fileName) // TODO replace with log
                    fileUtils.deleteFile(document.tenant, document.id, document.fileName)
                    docRepo.save(document.copy(status = DocumentStatus.FAILED.ordinal))
                    return@launch
                }

                processFile(document)
            }
        }
    }
}