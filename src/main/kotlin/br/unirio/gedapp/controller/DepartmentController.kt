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
    fun getDepartment(@PathVariable id : Long): ResponseEntity<Department> {
        val dept = deptSvc.getById(id)
        return ResponseEntity.ok(dept)
    }

    @GetMapping
    fun getAllDepartments(): ResponseEntity<Iterable<Department>> {
        val allDepts = deptRepo.findAll()
        return ResponseEntity.ok(allDepts)
    }

    @PostMapping
    fun addDepartment(@RequestBody dept: Department): ResponseEntity<Department> {
        val newDept = deptSvc.createNewDepartment(dept)
        return ResponseEntity.status(HttpStatus.CREATED).body(newDept)
    }

    @PatchMapping("/{id}")
    fun updateDepartment(@PathVariable id : Long, @RequestBody dept: Department): ResponseEntity<Department> {
        val modifiedDept = deptSvc.update(id, dept)
        return ResponseEntity.ok(modifiedDept)
    }

    @DeleteMapping("/{id}")
    fun deleteDepartment(@PathVariable id : Long): ResponseEntity<String> {
        deptSvc.deleteById(id)
        return ResponseEntity.ok("Department '$id' have been successfully deleted")
    }
}