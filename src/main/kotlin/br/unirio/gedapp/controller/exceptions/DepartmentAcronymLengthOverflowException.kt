package br.unirio.gedapp.controller.exceptions

class DepartmentAcronymLengthOverflowException(
    message: String = "Department acronym length too big"
) : RuntimeException(message)