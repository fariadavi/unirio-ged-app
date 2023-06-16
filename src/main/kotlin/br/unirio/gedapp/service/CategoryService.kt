package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver
import br.unirio.gedapp.configuration.web.tenant.TenantIdentifierResolver.Companion.DEFAULT_TENANT
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.controller.exceptions.UnauthorizedException
import br.unirio.gedapp.controller.exceptions.UnnamedCategoryException
import br.unirio.gedapp.domain.Category
import br.unirio.gedapp.domain.dto.CategoryDTO
import br.unirio.gedapp.repository.CategoryRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class CategoryService(
    val catRepo: CategoryRepository,
    val tenantResolver: TenantIdentifierResolver
) {
    private fun hasCurrentTenant() = tenantResolver.resolveCurrentTenantIdentifier() !== DEFAULT_TENANT

    fun existsById(id: Long): Boolean = hasCurrentTenant() && catRepo.existsById(id)

    fun getById(id: Long): Category =
        if (!hasCurrentTenant()) throw UnauthorizedException()
        else catRepo.findById(id).orElseThrow { ResourceNotFoundException() }

    fun findAll(): List<Category> =
        if (!hasCurrentTenant()) throw UnauthorizedException()
        else catRepo.findAll(Sort.by("name"))

    fun findChildrenCategory(categoryId: Long): List<Category> =
        if (!hasCurrentTenant()) throw UnauthorizedException()
        else catRepo.findAllByParentId(categoryId)

    fun create(category: CategoryDTO): Category =
        if (!hasCurrentTenant()) throw UnauthorizedException()
        else Category(
            name = category.name ?: throw UnnamedCategoryException(),
            parent = category.parent?.let { getById(it) }
        ).let { catRepo.save(it) }

    fun update(id: Long, newCategory: Category): Category {
        if (!hasCurrentTenant()) throw UnauthorizedException()

        var category = getById(id)

        if (newCategory.name.isNotBlank())
            category = category.copy(name = newCategory.name)

        if (newCategory.parent != null)
            category = category.copy(parent = newCategory.parent)

        return catRepo.save(category)
    }

    fun getCategoryAncestorsFlattened(category: Category) =
        if (!hasCurrentTenant()) throw UnauthorizedException()
        else getCategoryAncestors(category)
            .entries
            .sortedBy { it.key }
            .joinToString(separator = " > ") { it.value.name }

    /**
     * Returns a [MutableMap] representing the complete lineage of the desired category.
     * Each entry of the map consists of an [Int] key to [Category] value, representing all an ancestry levels from:
     *  0, which is the root, to
     *  N, which is the category received as parameter.
     */
    private fun getCategoryAncestors(category: Category): MutableMap<Int, Category> {
        var ancestry = mutableMapOf<Int, Category>()

        val parent = category.parent
        if (parent != null)
            ancestry = getCategoryAncestors(parent)

        ancestry[ancestry.size] = category

        return ancestry
    }
}