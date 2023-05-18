package br.unirio.gedapp.controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class UserAlreadyInvitedException(message: String = "User has already been invited") : RuntimeException(message)