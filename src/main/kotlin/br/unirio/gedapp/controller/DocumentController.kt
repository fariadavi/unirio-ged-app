package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.dto.DocumentDTO
import br.unirio.gedapp.domain.dto.GoogleDriveDocumentDTO
import br.unirio.gedapp.service.DocumentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@RestController
@RequestMapping("/documents")
class DocumentController(
    @Autowired private val docSvc: DocumentService
) {
    @GetMapping("/{id}")
    fun getDocument(@PathVariable id: String) =
        docSvc.createDTO(docSvc.getById(id))
            .let { ResponseEntity.ok(it) }

    @PostMapping(consumes = ["multipart/form-data"])
    fun addDocument(
        @RequestPart document: Document,
        @RequestPart file: MultipartFile
    ) = docSvc.insert(document, file)
        .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PostMapping("/import")
    suspend fun addDocumentList(
        @RequestBody gDocuments: List<GoogleDriveDocumentDTO>
    ) = docSvc.importGoogleDocs(gDocuments)
        .also { ResponseEntity.ok("Documents will be processed") }

    @PatchMapping("/{id}", consumes = ["multipart/form-data"])
    fun updateDocument(
        @PathVariable id: String,
        @RequestPart document: DocumentDTO,
        @RequestPart(required = false) file: MultipartFile?
    ) = docSvc.update(id, document, file)
        .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    fun deleteDocument(@PathVariable id: String) =
        docSvc.deleteById(id)
            .also { ResponseEntity.ok("Document '$id' have been successfully deleted") }

    @GetMapping("/search")
    fun searchDocuments(
        @RequestParam q: String,
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        @RequestParam(required = false) category: Long?,
        @RequestParam(required = false, defaultValue = "false") myDocuments: Boolean,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") minDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") maxDate: LocalDate?
    ) = docSvc.queryDocuments(q, page, pageSize, minDate, maxDate, category, myDocuments)

    @GetMapping("/{id}/download")
    fun downloadDocumentFile(@PathVariable id: String): ResponseEntity<ByteArray> {
        val doc = docSvc.getById(id)
        val file = docSvc.getFile(doc)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${doc.fileName}\"")
            .contentType(MediaType.parseMediaType(doc.mediaType ?: MediaType.APPLICATION_OCTET_STREAM.toString()))
            .body(file.readBytes())
    }
}