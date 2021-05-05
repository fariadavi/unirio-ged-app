package br.unirio.gedapp.service

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Category
import br.unirio.gedapp.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(val catRepo: CategoryRepository) {

    fun getById(id: Long): Category =
        catRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }

    /**
     * Returns a [MutableMap] representing the complete lineage of the desired category.
     * Each entry of the map consists of an [Int] key to [Category] value, representing all an ancestry levels from:
     *  0, which is the root, to
     *  N, which is the category received as parameter.
     */
    fun getCategoryAncestors(category: Category): MutableMap<Int, Category> {
        var ancestry = mutableMapOf<Int, Category>()

        val parent = category.parent
        if (parent != null)
            ancestry = getCategoryAncestors(parent)

        ancestry[ancestry.size] = category

        return ancestry
    }

    fun getCategoryAncestorsFlattened(category: Category) =
        getCategoryAncestors(category)
            .entries
            .sortedBy { it.key }
            .joinToString(separator = " > ") { it.value.name }
}