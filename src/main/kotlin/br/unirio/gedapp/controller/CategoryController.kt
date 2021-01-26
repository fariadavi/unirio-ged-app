package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Category
import br.unirio.gedapp.repository.CategoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(@Autowired private val categoryRepo: CategoryRepository) {

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id: Long): Category = categoryRepo.findById(id).get()

    @GetMapping
    fun getAllCategories(): Iterable<Category> = categoryRepo.findAll()

    @PostMapping
    fun addCategory(@RequestBody category: Category): Category = categoryRepo.save(category)

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long) = categoryRepo.deleteById(id)
}