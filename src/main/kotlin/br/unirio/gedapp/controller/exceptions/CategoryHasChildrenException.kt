package br.unirio.gedapp.controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
class CategoryHasChildrenException(message: String = "Categories which are parent other categories can't be deleted!") : RuntimeException(message)