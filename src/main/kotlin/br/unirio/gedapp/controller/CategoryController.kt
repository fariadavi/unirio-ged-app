package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Category
import br.unirio.gedapp.repository.CategoryRepository
import br.unirio.gedapp.service.CategoryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    @Autowired private val catSvc: CategoryService,
    @Autowired private val catRepo: CategoryRepository
) {

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id: Long): Category =
        catSvc.getById(id)

    @GetMapping
    fun getAllCategoriesFlattened(): Iterable<Category> {
        val allCategories = catRepo.findAll()
        allCategories.forEach { it.fullName = catSvc.getCategoryAncestorsFlattened(it) }
        return allCategories
    }

    @PostMapping
    fun addCategory(@RequestBody category: Category): Category = catRepo.save(category)

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long) = catRepo.deleteById(id)
}