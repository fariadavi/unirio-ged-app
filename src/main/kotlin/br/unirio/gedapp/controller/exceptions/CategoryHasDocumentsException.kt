package br.unirio.gedapp.controller.exceptions

class CategoryHasDocumentsException(
    message: String = "Categories with documents can't be deleted!"
) : RuntimeException(message)