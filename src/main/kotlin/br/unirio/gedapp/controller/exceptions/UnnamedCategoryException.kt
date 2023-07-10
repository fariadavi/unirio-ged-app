package br.unirio.gedapp.controller.exceptions

class UnnamedCategoryException(
    message: String = "Category must have a name"
) : RuntimeException(message)