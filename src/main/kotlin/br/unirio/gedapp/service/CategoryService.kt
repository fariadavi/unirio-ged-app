package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver.Companion.DEFAULT_TENANT
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Category
import br.unirio.gedapp.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    val catRepo: CategoryRepository,
    val tenantResolver: TenantIdentifierResolver
) {

    private fun hasCurrentTenant() = tenantResolver.resolveCurrentTenantIdentifier() !== DEFAULT_TENANT

    fun existsById(id: Long): Boolean = hasCurrentTenant() && catRepo.existsById(id)

    fun getById(id: Long): Category {
        if (!hasCurrentTenant())
            throw ResourceNotFoundException()

        return catRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }
    }

    fun findAll(): List<Category> {
        if (!hasCurrentTenant())
            return emptyList()

        return catRepo.findAll()
    }

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