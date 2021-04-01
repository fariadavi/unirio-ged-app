package br.unirio.gedapp.controller

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.repository.DocumentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/documents")
class DocumentController(
    @Autowired private val docRepo: DocumentRepository,
    @Autowired private val tenantResolver: TenantIdentifierResolver
) {

    @GetMapping("/{id}")
    fun getDocument(@PathVariable id: String): Document = docRepo.findById(id).get()

    @GetMapping
    fun getAllDocuments(): Iterable<Document> = docRepo.findAll()

    @PostMapping
    fun addDocument(@RequestBody doc: Document): Document = docRepo.save(doc)

    @DeleteMapping("/{id}")
    fun deleteDocument(@PathVariable id: String) = docRepo.deleteById(id)

    @GetMapping("/search")
    fun searchDocuments(@RequestParam q: String) =
        docRepo.findByTenantAndContentMatches(
            tenantResolver.resolveCurrentTenantIdentifier(),
            q)
}