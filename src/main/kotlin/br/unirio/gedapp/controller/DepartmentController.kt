package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.repository.DepartmentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/departments")
class DepartmentController(@Autowired private val deptRepo: DepartmentRepository) {

    @GetMapping("/{id}")
    fun getDepartment(@PathVariable id: Long): Department = deptRepo.findById(id).get()

    @GetMapping
    fun getAllDepartments(): Iterable<Department> = deptRepo.findAll()

    @PostMapping
    fun addDepartment(@RequestBody department: Department): Department = deptRepo.save(department)

    @DeleteMapping("/{id}")
    fun deleteDepartment(@PathVariable id: Long) = deptRepo.deleteById(id)
}