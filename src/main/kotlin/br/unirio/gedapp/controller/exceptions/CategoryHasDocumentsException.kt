package br.unirio.gedapp.controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
class CategoryHasDocumentsException(message: String = "Categories with documents can't be deleted!") : RuntimeException(message)