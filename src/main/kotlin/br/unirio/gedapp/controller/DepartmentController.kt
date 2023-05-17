package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.domain.dto.DepartmentDTO
import br.unirio.gedapp.service.DepartmentService
import br.unirio.gedapp.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/departments")
class DepartmentController(
    @Autowired private val deptSvc: DepartmentService,
    @Autowired private val userSvc: UserService,
) {

    @GetMapping("/{id}")
    fun getDepartment(@PathVariable id : Long): ResponseEntity<Department> {
        val dept = deptSvc.getById(id)
        return ResponseEntity.ok(dept)
    }

    @GetMapping
    fun getAllDepartments(): ResponseEntity<Iterable<DepartmentDTO>> {
        val allDepts = deptSvc.findAllDepartments()
        return ResponseEntity.ok(allDepts)
    }

    @PostMapping
    fun addDepartment(@RequestBody dept: Department): ResponseEntity<Department> {
        val newDept = deptSvc.createNewDepartment(dept)

        val user = userSvc.getCurrentUser()
        userSvc.addUserToDepartment(user, newDept, user.currentDepartment == null)

        return ResponseEntity.status(HttpStatus.CREATED).body(newDept)
    }

    @PatchMapping("/{id}")
    fun updateDepartment(@PathVariable id : Long, @RequestBody dept: Department): ResponseEntity<Department> {
        val modifiedDept = deptSvc.update(id, dept)
        return ResponseEntity.ok(modifiedDept)
    }

    @PatchMapping
    fun updateDepartments(@RequestBody editedDepartments: List<Department>): ResponseEntity<List<Department>> {
        val (modifiedDeptList, numErrors) = deptSvc.batchUpdate(editedDepartments)

        val response = when (numErrors) {
            0 -> ResponseEntity.status(HttpStatus.OK)
            modifiedDeptList.size -> ResponseEntity.status(HttpStatus.CONFLICT)
            else -> ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
        }
        return response.body(modifiedDeptList)
    }

    @DeleteMapping("/{id}")
    fun deleteDepartment(@PathVariable id : Long): ResponseEntity<String> {
        deptSvc.deleteById(id)
        return ResponseEntity.ok("Department '$id' have been successfully deleted")
    }
}