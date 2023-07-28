package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver.Companion.DEFAULT_TENANT
import br.unirio.gedapp.configuration.yml.StorageConfig
import br.unirio.gedapp.configuration.yml.TesseractConfig
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.controller.exceptions.UnauthorizedException
import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import br.unirio.gedapp.domain.Permission
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.parser.ocr.TesseractOCRConfig
import org.apache.tika.sax.BodyContentHandler
import org.apache.tika.sax.WriteOutContentHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

@Service
class DocumentService(
    @Autowired storageConfig: StorageConfig,
    @Autowired val tesseractConfig: TesseractConfig,
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

    fun insert(document: DocumentDTO, file: MultipartFile? = null): Document {
        var newDoc = Document(
            tenant = tenantResolver.resolveCurrentTenantIdentifier(),
            title = document.title,
            date = document.date,
            summary = document.summary,
            category = document.categoryId,
            registeredBy = userSvc.getCurrentUser().id,
            registeredAt = LocalDateTime.now()
        )

        if (file != null)
            newDoc = newDoc.copy(fileName = file.originalFilename!!)

        newDoc = docRepo.save(newDoc)

        if (file != null)
            updateDocumentFile(newDoc, file)

        return newDoc
    }

    fun update(docId: String, newDataDoc: DocumentDTO, file: MultipartFile?): Document {
        val currentUser = userSvc.getCurrentUser()
        var existingDoc = getById(docId)
        if (existingDoc.registeredBy != currentUser.id
            || currentUser.userPermission == null
            || !currentUser.userPermission.permissions.contains(Permission.EDIT_DOCS_OTHERS)
        )
            throw UnauthorizedException()

        if (newDataDoc.title.isNotBlank())
            existingDoc = existingDoc.copy(title = newDataDoc.title)

        if (newDataDoc.summary.isNotBlank())
            existingDoc = existingDoc.copy(summary = newDataDoc.summary)

        if (newDataDoc.date != null)
            existingDoc = existingDoc.copy(date = newDataDoc.date)

        if (newDataDoc.categoryId != -1L)
            existingDoc = existingDoc.copy(category = newDataDoc.categoryId)

        if (file?.originalFilename != null) {
            val existingDocFile = getFile(existingDoc)

            if (existingDoc.status == DocumentStatus.FAILED_PROCESSING.ordinal
                || existingDoc.status == DocumentStatus.EMPTY_CONTENT.ordinal
                || !existingDocFile.readBytes().contentEquals(file.bytes)
            ) {
                existingDoc = existingDoc.copy(status = DocumentStatus.PENDING.ordinal, content = "", mediaType = null)

                updateDocumentFile(existingDoc, file, existingDocFile)
            }

            existingDoc = existingDoc.copy(fileName = file.originalFilename!!)
        }

        return docRepo.save(existingDoc)
    }

    fun updateDocumentFile(doc: Document, file: MultipartFile, currentFile: File? = null) =
        fileUtils.transferFile(file, doc.tenant, doc.id!!).let {
            CoroutineScope(EmptyCoroutineContext).launch { // process file content asynchronously
                if (currentFile != null)
                    fileUtils.deleteFile(doc.tenant, currentFile.name)

                processFile(doc.copy(fileName = file.originalFilename!!))

                //TODO somehow notify user that the file status has been updated for either success or fail
            }
        }

    private fun processFile(doc: Document) {
        val filepath = fileUtils.getFilePath(doc.tenant, doc.id!!, doc.fileName)

        logger.info("Processing file at '$filepath' for Document '${doc.title}' (${doc.id})")

        var docCopy = docRepo.save(doc.copy(status = DocumentStatus.PROCESSING.ordinal))

        val (docContent, mediaType, extractionStatus) = extractContents(filepath)
        docCopy = docCopy.copy(content = docContent, mediaType = mediaType, status = extractionStatus.ordinal)

        if (extractionStatus === DocumentStatus.FAILED_PROCESSING)
            docCopy = doc.copy(content = "", mediaType = null)

        docRepo.save(docCopy)
    }

    private fun extractContents(filepath: Path): Triple<String, String, DocumentStatus> {
        var docContent = ""
        var mediaType = ""
        var extractionStatus = DocumentStatus.PROCESSED

        try {
            val config = TesseractOCRConfig()
            config.language = tesseractConfig.language

            val metadata = Metadata()
            docContent = TikaInputStream.get(filepath, metadata).use {
                val parser = AutoDetectParser()

                val context = ParseContext()
                context.set(TesseractOCRConfig::class.java, config)
                context.set(Parser::class.java, parser)

                val handler = WriteOutContentHandler(100_000) // set default write limit as 100k characters

                parser.parse(it, BodyContentHandler(handler), metadata, context)

                return@use handler.toString()
            }

            mediaType = TikaInputStream.get(filepath, metadata).use {
                DefaultDetector().detect(it, metadata).toString()
            }

            if (docContent.isBlank()) extractionStatus = DocumentStatus.EMPTY_CONTENT
        } catch (e: Exception) {
            extractionStatus = DocumentStatus.FAILED_PROCESSING
            logger.error("Failed to parse file at $filepath", e)
        }

        return Triple(docContent, mediaType, extractionStatus)
    }

    fun delete(document: Document) {
        docRepo.deleteById(document.id!!)

        fileUtils.deleteFile(document.tenant, document.id, document.fileName)
    }

    fun deleteById(id: String) {
        val currentUser = userSvc.getCurrentUser()
        val existingDoc = getById(id)
        if (existingDoc.registeredBy != currentUser.id
            || currentUser.userPermission == null
            || !currentUser.userPermission.permissions.contains(Permission.DELETE_DOCS_OTHERS)
        )
            throw UnauthorizedException()

        delete(existingDoc)
    }

    fun deleteAllByTenant(tenant: String) =
        docRepo.deleteAllByTenant(tenant)

    fun queryDocuments(
        queryString: String,
        page: Int,
        pageSize: Int,
        minDate: LocalDate?,
        maxDate: LocalDate?,
        categoryId: Long?,
        onlyMyDocs: Boolean,
        status: DocumentStatus?
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
            maxDate,
            status
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

    fun getDocCountByTenant(tenant: String) = docRepo.countByTenant(tenant)

    fun updateDocsTenantAcronym(tenant: String, newAcronym: String) =
        docRepo.updateDocsTenantAcronym(tenant, newAcronym)

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

            //TODO somehow notify user that the file status has been updated for either success or fail
        }.invokeOnCompletion {
            logger.info("Finished importing ${gDocuments.size} documents on tenant $currentTenant")
        }
    }

    private suspend fun saveGoogleDoc(
        googleDoc: GoogleDriveDocumentDTO,
        userId: Long,
        tenant: String
    ): Document? {
        logger.info("Creating document for Google Doc '${googleDoc.name}' (${googleDoc.id})")

        return try {
            Document(
                tenant = tenant,
                fileName = googleDoc.name.trim() + if (googleDoc.type == "document") ".pdf" else "",
                title = googleDoc.name.trim(),
                summary = googleDoc.description.takeUnless { it.isNullOrBlank() },
                mediaType = googleDoc.mimeType,
                category = googleDoc.category,
                date = LocalDate.parse(googleDoc.date),
                registeredAt = LocalDateTime.now(),
                registeredBy = userId
            ).let { docRepo.save(it) }
        } catch (e: Exception) {
            logger.info("Unable to create document for Google Doc '${googleDoc.name}' (${googleDoc.id})", e)
            null
        }
    }

    private suspend fun importGoogleFiles(docMap: Map<Document, GoogleDriveDocumentDTO>) = coroutineScope {
        logger.info("Importing files from Google Drive of '${docMap.values.first().email}'")

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

                try {
                    val filepath = fileUtils.getFilePath(document.tenant, document.id!!, document.fileName)
                    FileOutputStream(filepath.toFile())
                } catch (e: Exception) {
                    logger.error("Error creating file for document '${document.fileName}' on tenant '${document.tenant}'", e)
                    docRepo.save(document.copy(status = DocumentStatus.FAILED_IMPORT.ordinal, fileName = "", statusDetails = e.message))
                    return@launch

                }.use {
                    outputStream ->
                    logger.info("Fetching document '${driveDoc.name}' (${driveDoc.id}) from Google Drive")

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
                        logger.error("Unable to download file '${driveDoc.name}' (${driveDoc.id}) from Google Drive", e)
                        fileUtils.deleteFile(document.tenant, document.id, document.fileName)
                        docRepo.save(document.copy(status = DocumentStatus.FAILED_IMPORT.ordinal, fileName = "", statusDetails = e.message))
                        return@launch

                    } catch (e: Exception) {
                        logger.error("Error retrieving file '${driveDoc.name}' (${driveDoc.id}) from Google Drive", e)
                        fileUtils.deleteFile(document.tenant, document.id, document.fileName)
                        docRepo.save(
                            document.copy(
                                status = DocumentStatus.FAILED_IMPORT.ordinal,
                                fileName = "",
                                statusDetails = e.message
                            )
                        )
                        return@launch
                    }
                }

                processFile(document)
            }
        }
    }
}