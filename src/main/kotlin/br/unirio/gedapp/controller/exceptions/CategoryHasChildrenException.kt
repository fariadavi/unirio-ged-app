package br.unirio.gedapp.controller.exceptions

class CategoryHasChildrenException(
    message: String = "Categories which are parent other categories can't be deleted!"
) : RuntimeException(message)