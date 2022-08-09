package br.unirio.gedapp.service

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.domain.Permission
import br.unirio.gedapp.domain.User
import br.unirio.gedapp.repository.DepartmentRepository
import br.unirio.gedapp.repository.DocumentRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class DepartmentService(
    val deptRepo: DepartmentRepository,
    val tenantSvc: TenantService,
    val docRepo: DocumentRepository
) {

    fun getById(id: Long): Department =
        deptRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }

    @Transactional
    fun createNewDepartment(dept: Department): Department {
        val savedDept: Department = deptRepo.save(dept)
        tenantSvc.initDatabase(savedDept.acronym!!.toLowerCase())
        return savedDept
    }

    @Transactional
    fun update(deptId: Long, newDataDept: Department): Department {
        var existingDept = getById(deptId)
        val currentAcronym = existingDept.acronym

        if (!newDataDept.name.isNullOrBlank())
            existingDept = existingDept.copy(name = newDataDept.name)

        if (!newDataDept.acronym.isNullOrBlank())
            existingDept = existingDept.copy(acronym = newDataDept.acronym)

        val updatedDept = deptRepo.save(existingDept)

        if (currentAcronym!!.toLowerCase() != updatedDept.acronym!!.toLowerCase()) {
            tenantSvc.renameSchema(currentAcronym, updatedDept.acronym)

//            val allDeptDocs = docRepo.findAllByTenant(currentAcronym)
            //TODO: rename tenant for all docs found
        }

        return updatedDept
    }

    fun deleteById(deptId: Long) = delete(getById(deptId))

    @Transactional
    fun delete(dept: Department) {
        val numDocs = docRepo.countByTenant(dept.acronym!!)
        if (numDocs > 0)
            throw DataIntegrityViolationException("There are $numDocs documents associated with this department. It's not possible to delete an active department.")

        try {
            deptRepo.delete(dept)
        } catch (e: DataIntegrityViolationException) {
            throw DataIntegrityViolationException("There are users associated with this department. It's not possible to delete an active department.")
        }

        tenantSvc.dropSchema(dept.acronym)
    }

    fun batchUpdate(editedDepartments: List<Department>): Pair<List<Department>, Int> {
        var numErrors = 0
        val modifiedDepartments = mutableListOf<Department>()

        editedDepartments.forEach {
            try {
                val modifiedDept = update(it.id, it)
                modifiedDepartments.add(modifiedDept)
            } catch (e : Exception) {
                modifiedDepartments.add(getById(it.id))
                numErrors++
            }
        }

        return Pair(modifiedDepartments, numErrors)
    }
}