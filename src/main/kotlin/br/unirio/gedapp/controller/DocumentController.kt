package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.repository.DocumentRepository
import br.unirio.gedapp.service.DocumentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @PostMapping
    fun addDocument(@RequestBody doc: Document): ResponseEntity<Document> {
        val createdDoc = docRepo.save(doc)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDoc)
    }

    @PatchMapping("/{id}")
    fun updateDocument(@PathVariable id : String, @RequestBody newDataDoc: Document): ResponseEntity<Document> {
        val modifiedDoc = docSvc.update(id, newDataDoc)
        return ResponseEntity.ok(modifiedDoc)
    }

    @DeleteMapping("/{id}")
    fun deleteDocument(@PathVariable id: String): ResponseEntity<String> {
        docSvc.deleteById(id)
        return ResponseEntity.ok("Document '$id' have been successfully deleted")
    }

    @GetMapping("/search")
    fun searchDocuments(@RequestParam q: String) =
        docSvc.queryDocuments(q)
}