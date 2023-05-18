package br.unirio.gedapp.controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException(message: String = "Unauthorized Request") : RuntimeException(message)