package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.repository.DocumentRepository
import br.unirio.gedapp.service.DocumentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/documents")
class DocumentController(
    @Autowired private val docSvc: DocumentService,
    @Autowired private val docRepo: DocumentRepository
) {

    @GetMapping("/{id}")
    fun getDocument(@PathVariable id: String) =
        ResponseEntity.ok(
            docSvc.getById(id))

    @PostMapping(consumes = ["multipart/form-data"])
    fun addDocument(
        @RequestPart document: Document,
        @RequestPart file: MultipartFile
    ) = ResponseEntity.status(HttpStatus.CREATED).body(
            docSvc.insert(document, file))

    @PatchMapping("/{id}", consumes = ["multipart/form-data"])
    fun updateDocument(
        @PathVariable id: String,
        @RequestPart document: Document,
        @RequestPart(required = false) file: MultipartFile?
    ) = ResponseEntity.ok(
            docSvc.update(id, document, file))

    @DeleteMapping("/{id}")
    fun deleteDocument(@PathVariable id: String): ResponseEntity<String> {
        docSvc.deleteById(id)
        return ResponseEntity.ok("Document '$id' have been successfully deleted")
    }

    @GetMapping("/search")
    fun searchDocuments(@RequestParam q: String) =
        docSvc.queryDocuments(q)
}