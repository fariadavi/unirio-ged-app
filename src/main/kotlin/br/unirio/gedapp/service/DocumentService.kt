package br.unirio.gedapp.service

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.repository.DocumentRepository
import org.springframework.stereotype.Service

@Service
class DocumentService(val docRepo: DocumentRepository) {

    fun getById(id: String): Document =
        docRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }

    fun update(docId: String, newDataDoc: Document): Document {
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
}