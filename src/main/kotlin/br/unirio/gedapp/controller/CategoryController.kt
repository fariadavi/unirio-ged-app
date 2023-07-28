package br.unirio.gedapp.controller

import br.unirio.gedapp.controller.exceptions.CategoryHasChildrenException
import br.unirio.gedapp.controller.exceptions.CategoryHasDocumentsException
import br.unirio.gedapp.domain.Category
import br.unirio.gedapp.domain.dto.CategoryDTO
import br.unirio.gedapp.repository.CategoryRepository
import br.unirio.gedapp.service.CategoryService
import br.unirio.gedapp.service.DocumentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    @Autowired private val catSvc: CategoryService,
    @Autowired private val catRepo: CategoryRepository,
    @Autowired private val docSvc: DocumentService
) {
    @GetMapping("/{id}")
    fun getCategory(@PathVariable id: Long): ResponseEntity<Category> =
        catSvc.getById(id)
            .let { ResponseEntity.ok(it) }

    @GetMapping
    fun getAllCategories(
        @RequestParam(required = false, defaultValue = "false") fullName: Boolean,
        @RequestParam(required = false, defaultValue = "false") numDocs: Boolean
    ): ResponseEntity<Iterable<CategoryDTO>> {
        val mapDocsByCategory = if (numDocs) docSvc.getMapOfCategoriesWithDocCount() else emptyMap()
        return catSvc.findAll().map { cat ->
            CategoryDTO(
                cat,
                if (fullName) catSvc.getCategoryAncestorsFlattened(cat) else null,
                if (numDocs) mapDocsByCategory.entries.firstOrNull { e -> e.key == cat.id }?.value else null,
            )
        }.let { ResponseEntity.ok(it) }
    }

    @Secured("MANAGE_CATEGORIES")
    @PostMapping
    fun addCategory(@RequestBody category: CategoryDTO): ResponseEntity<Category> =
        catSvc.create(category)
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @Secured("MANAGE_CATEGORIES")
    @PatchMapping("/{id}")
    fun editCategory(@PathVariable id: Long, @RequestBody category: Category): ResponseEntity<Category> =
        catSvc.update(id, category)
            .let { ResponseEntity.ok(it) }

    @Secured("MANAGE_CATEGORIES")
    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Category> {
        if (catSvc.findChildrenCategory(id).isNotEmpty()) throw CategoryHasChildrenException()
        if (docSvc.getDocCountByCategory(id) > 0) throw CategoryHasDocumentsException()
        return catRepo.deleteById(id)
            .let { ResponseEntity.noContent().build() }
    }
}