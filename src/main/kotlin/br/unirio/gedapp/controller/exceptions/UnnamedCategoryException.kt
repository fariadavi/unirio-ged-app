package br.unirio.gedapp.controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
class UnnamedCategoryException(message: String = "Category must have a name") : RuntimeException(message)