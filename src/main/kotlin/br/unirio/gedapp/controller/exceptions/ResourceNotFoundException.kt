package br.unirio.gedapp.controller.exceptions

class ResourceNotFoundException(
    message: String = "Resource Not Found"
) : RuntimeException(message)