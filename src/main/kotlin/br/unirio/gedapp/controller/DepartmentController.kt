package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.repository.DepartmentRepository
import br.unirio.gedapp.service.DepartmentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/departments")
class DepartmentController(
    @Autowired private val deptSvc: DepartmentService,
    @Autowired private val deptRepo: DepartmentRepository
) {

    @GetMapping("/{id}")
    fun getDepartment(@PathVariable id: Long): Department = deptRepo.findById(id).get()

    @GetMapping
    fun getAllDepartments(): Iterable<Department> = deptRepo.findAll()

    @PostMapping
    fun addDepartment(@RequestBody dept: Department): ResponseEntity<Department> {
        val newDept = deptSvc.createNewDepartment(dept)
        return ResponseEntity.status(HttpStatus.CREATED).body(newDept)
    }

    @DeleteMapping("/{id}")
    fun deleteDepartment(@PathVariable id: Long) = deptRepo.deleteById(id)
}