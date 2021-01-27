package br.unirio.gedapp.service

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.repository.DepartmentRepository
import br.unirio.gedapp.repository.DocumentRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
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
        val saved: Department = deptRepo.save(dept)
        tenantSvc.initDatabase(saved.acronym.toLowerCase())
        return saved
    }

    @Transactional
    fun update(deptId: Long, newDataDept: Department): Department {
        var updatedDept = getById(deptId)
        val currentAcronym = updatedDept.acronym

        if (newDataDept.name.isNotBlank())
            updatedDept = updatedDept.copy(name = newDataDept.name)

        if (newDataDept.acronym.isNotBlank())
            updatedDept = updatedDept.copy(acronym = newDataDept.acronym)

        updatedDept = deptRepo.save(updatedDept)


        if (currentAcronym != updatedDept.acronym) {
            tenantSvc.renameSchema(currentAcronym, updatedDept.acronym)

            val allDeptDocs = docRepo.findAllByTenant(currentAcronym)
            //TODO: rename tenant for all docs found
        }

        return updatedDept
    }

    fun deleteById(deptId: Long) = delete(getById(deptId))

    @Transactional
    fun delete(dept: Department) {
        val numDocs = docRepo.countByTenant(dept.acronym)
        if (numDocs > 0)
            throw DataIntegrityViolationException("There are $numDocs documents associated with this department. It's not possible to delete an active department.")

        try {
            deptRepo.delete(dept)
        } catch (e : DataIntegrityViolationException) {
            throw DataIntegrityViolationException("There are users associated with this department. It's not possible to delete an active department.")
        }

        tenantSvc.dropSchema(dept.acronym)
    }
}