package br.unirio.gedapp.controller.exceptions

class UnauthorizedException(
    message: String = "Unauthorized Request"
) : RuntimeException(message)