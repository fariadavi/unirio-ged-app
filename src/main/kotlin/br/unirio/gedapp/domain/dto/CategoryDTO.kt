package br.unirio.gedapp.domain.dto

import br.unirio.gedapp.domain.Category

data class CategoryDTO(
    var id: Long?,
    var name: String?,
    var parent: Long?,
    var fullName: String?,
    var numDocs: Long?
) {
    constructor(category: Category, fullName: String?, numDocs: Long?) : this(
        category.id,
        category.name,
        category.parent?.id,
        fullName,
        numDocs
    )
}
